package com.vv.finance.investment.bg.stock.info.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockRelationDto;
import com.vv.finance.common.enums.StockRelationBizEnum;
import com.vv.finance.common.enums.StockRelationStatusEnum;
import com.vv.finance.common.utils.StockIdUtil;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.investment.bg.stock.info.service.HkStockRelationService;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.utils.TimeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @author Lekt
 * @description 针对表【t_hk_stock_relation(股票关系表)】的数据库操作Service实现
 * @createDate 2024-06-25 16:24:49
 */
@Slf4j
@Service
public class HkStockRelationServiceImpl extends ServiceImpl<HkStockRelationMapper, HkStockRelation> implements HkStockRelationService {

    private static final String RELATION_REMARK = "码表同步";

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private HkStockRelationMapper stockRelationMapper;

    @Resource
    private RedisLockRegistry redisLockRegistry;


    @Resource
    private StockInfoApi stockInfoApi;

    @Resource
    private StockCache stockCache;

    @Resource
    private UtsInfoService utsInfoService;

    @Resource
    private RedisClient redisClient;

    @Override
    public void saveNewRelations(List<String> codeList) {
        log.info("StockRelationService saveNewRelations codeList: {}", CollUtil.size(codeList));
        TimeInterval timeInterval = new TimeInterval();

        // 所有relation
        List<HkStockRelation> dbStockRelations = stockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getInnerCode, codeList));
        // 所有code map
        Map<String, HkStockRelation> codeRelationMap = CollUtil.toMap(dbStockRelations, new HashMap<>(), HkStockRelation::getInnerCode);
        // 状态为0
        Collection<HkStockRelation> validStockRelations = CollUtil.filterNew(dbStockRelations, hsr -> ObjectUtil.equal(StockRelationStatusEnum.NORMAL.getCode(), hsr.getSecurityStatus()));
        // 状态为0 map
        List<String> validInnerCodes = CollUtil.map(validStockRelations, HkStockRelation::getInnerCode, true);

        // 差集，新增(不在关联表中或状态不为0的code)
        Collection<String> subCodes = CollUtil.subtract(codeList, validInnerCodes);
        // 交集，更新
        Collection<String> interCodes = CollUtil.intersection(codeList, validInnerCodes);

        // 所有临时股票
        List<ReuseTempDTO> allTempDtoList = utsInfoService.findAllTempStocks();
        Map<String, ReuseTempDTO> allCodeTempMap = CollUtil.toMap(allTempDtoList, new HashMap<>(), ReuseTempDTO::getCode);
        // 所有股权股票
        List<StockRightsDTO> allStockRights = utsInfoService.getAllStockRights();
        Map<String, StockRightsDTO> allCodeRightMap = CollUtil.toMap(allStockRights, new HashMap<>(), StockRightsDTO::getCode);
        // 所有不在并行交易期临时股票
        List<String> allUnTradeTempSocks = utsInfoService.findAllUnTradeTempSocks(DateUtil.date());
        // 所有不在交易期间的股权股票
        List<StockRightsDTO> allUnTradeRightStocks = utsInfoService.getUnTradingStockRights(DateUtil.date());
        List<String> allUnTradeRightCodes = CollUtil.map(allUnTradeRightStocks, StockRightsDTO::getCode, true);
        // 当日码表
        Set<String> todayCodeSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET);

        // 股权股票，不在交易期间；临时股票，不在并行交易期；不在当日码表，不加入关联表
        Collection<String> abandonRightStocks = CollUtil.subtract(allUnTradeRightCodes, todayCodeSet);
        Collection<String> abandonTempStocks = CollUtil.subtract(allUnTradeTempSocks, abandonRightStocks);
        log.info("StockRelationService saveNewRelations abandonRightStocks: {}", ArrayUtil.toString(abandonRightStocks.toArray()));
        log.info("StockRelationService saveNewRelations abandonTempStocks: {}", ArrayUtil.toString(abandonTempStocks.toArray()));

        // 新增关系表
        if (CollUtil.isNotEmpty(subCodes)) {
            // 差集，新增
            log.info("StockRelationService saveNewRelations subCodes: {}", ArrayUtil.toString(subCodes.toArray()));
            // 新增关系表
            subCodes.forEach(code -> {
                ComStockRelationDto relation = ComStockRelationDto.builder().sourceCode(code).innerCode(code).bizType(StockRelationBizEnum.SYNC.getCode()).remark(RELATION_REMARK).build();
                if (codeRelationMap.containsKey(code)) {
                    log.info("StockRelationService codeRelationMap exist: {}", code);
                    return;
                }
                if (CollUtil.contains(abandonRightStocks, code)) {
                    // 已废弃临时股票
                    StockRightsDTO rightsDTO = allCodeRightMap.get(code);
                    relation.setBizTime(String.valueOf(TimeConvertUtil.getYmdByTimeStamp(rightsDTO.getEndListingDate())));
                    relation.setSecurityStatus(StockRelationStatusEnum.INVALID.getCode());
                    relation.setRemark("【股权股票】废弃股权股票新增到关联表");
                }
                if (CollUtil.contains(abandonTempStocks, code)) {
                    // 已废弃临时股票
                    ReuseTempDTO tempDTO = allCodeTempMap.get(code);
                    relation.setBizTime(String.valueOf(TimeConvertUtil.getYmdByTimeStamp(tempDTO.getEndTime())));
                    relation.setSecurityStatus(StockRelationStatusEnum.INVALID.getCode());
                    relation.setRemark("【临时股票】废弃临时股票新增到关联表");
                }
                buildStockId(relation);
            });
        }

        if (CollUtil.isNotEmpty(interCodes)) {
            // 更新临时股票状态为失效
            Collection<String> updateRightCodes = CollUtil.intersection(interCodes, abandonRightStocks);
            Collection<String> updateTempCodes = CollUtil.intersection(interCodes, updateRightCodes);
            List<String> updateCodes = CollUtil.unionAll(updateRightCodes, updateTempCodes);
            log.info("StockRelationService saveNewRelations updateCodes: {}", ArrayUtil.toString(updateCodes.toArray()));
            List<HkStockRelation> updateList = CollUtil.map(updateCodes, code -> {
                HkStockRelation relation = codeRelationMap.get(code);
                if (CollUtil.contains(updateRightCodes, code)) {
                    StockRightsDTO rightsDTO = allCodeRightMap.get(code);
                    relation.setBizTime(String.valueOf(TimeConvertUtil.getYmdByTimeStamp(rightsDTO.getEndListingDate())));
                    relation.setRemark(getSaveRemark(relation.getRemark(), "【股权股票】废弃股权股票更新到关联表"));
                    relation.setUpdateTime(LocalDateTime.now());
                } else if (CollUtil.contains(updateTempCodes, code)) {
                    ReuseTempDTO tempDTO = allCodeTempMap.get(code);
                    relation.setBizTime(String.valueOf(TimeConvertUtil.getYmdByTimeStamp(tempDTO.getEndTime())));
                    relation.setRemark(getSaveRemark(relation.getRemark(), "【临时股票】废弃临时股票更新到关联表"));
                    relation.setUpdateTime(LocalDateTime.now());
                } else {
                    return null;
                }
                relation.setSecurityStatus(StockRelationStatusEnum.INVALID.getCode());
                return relation;
            }, true);
            this.updateBatchById(updateList);
        }

        // 刷新缓存
        stockCache.updateStockSimpleInfo();
        // stockInfoApi.sendUpdateMessage();

        log.info("StockRelationService saveNewRelations end, cost: {}", timeInterval.interval() / 1000.0);
    }

    @Override
    public Map<String, Long> updateStockRelations(List<ComStockRelationDto> relationDtoList) {
        log.info("StockRelationService updateStockRelation size: {}", CollUtil.size(relationDtoList));
        HashMap<String, Long> resultMap = new HashMap<>(CollUtil.size(relationDtoList));
        relationDtoList.forEach(relationDto -> {
            try {
                Long stockId = updateStockRelation(relationDto);
                resultMap.put(relationDto.getSourceCode(), stockId);
            } catch (Exception e) {
                log.error("StockRelationService updateStockRelation occurs error", e);
            }
        });
        // 刷新缓存
        // stockCache.updateStockSimpleInfo();
        stockInfoApi.sendUpdateMessage();
        return resultMap;
    }

    @Override
    public Long buildStockId(ComStockRelationDto relationDto) {
        log.info("StockRelationService buildStockId relationDto: {}", JSON.toJSONString(relationDto));
        TimeInterval timeInterval = new TimeInterval();
        Long stockId = null;
        String innerCode = relationDto.getInnerCode();
        Lock lock = redisLockRegistry.obtain(RedisKeyConstants.BG_STOCK_RELATION_SAVE_LOCK);
        boolean locked = false;
        try {
            locked = lock.tryLock(15, TimeUnit.SECONDS);
            if (locked) {
                stockId = saveStockDefineAndRelation(relationDto);
            }
        } catch (InterruptedException e) {
            log.error("构建股票关系异常", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
            log.info("构建股票关系, code: {},stockId:{} cost: {}", innerCode, stockId, timeInterval.interval() / 1000.0);
        }

        // 同步融聚汇sdk，不放在锁中操作
        if(ObjectUtil.equals(StockRelationBizEnum.SIMULATE.getCode(), relationDto.getBizType())){
            String code = relationDto.getSourceCode().replace("-t","");
            StockDefine oldDefine = stockDefineService.getOne(Wrappers.<StockDefine>lambdaQuery().eq(StockDefine::getCode, code).last("limit 1"));
            if (ObjectUtil.isNotEmpty(oldDefine)) {
                StockDefine reuseDefine = BeanUtil.copyProperties(oldDefine, StockDefine.class);
                reuseDefine.setId(null);
                reuseDefine.setCode(relationDto.getSourceCode());
                //1、落库
                stockDefineService.save(reuseDefine);
                // 2. 刷新缓存
                // stockCache.updateStockSimpleInfo();
                stockInfoApi.sendUpdateMessage();
            }
        } else if (!ObjectUtil.equals(StockRelationBizEnum.SYNC.getCode(), relationDto.getBizType())) {
            // 码表同步时，不更新基本信息
            updateStockInfo(innerCode);
        }

        return stockId;
    }

    @Override
    public Map<String, Long> selectStockIdByCodes(List<String> codes) {
        LambdaQueryWrapper<HkStockRelation> wrapper = Wrappers.<HkStockRelation>lambdaQuery().select(HkStockRelation::getStockId, HkStockRelation::getInnerCode)
                .eq(HkStockRelation::getSecurityStatus, StockRelationStatusEnum.NORMAL.getCode()).in(CollUtil.isNotEmpty(codes), HkStockRelation::getInnerCode, codes);
        List<HkStockRelation> stockRelations = this.list(wrapper);
        return stockRelations.stream().collect(Collectors.toMap(HkStockRelation::getInnerCode, HkStockRelation::getStockId, (o, v) -> v));
    }

    @Override
    public Map<String, String> selectReuseCodeMap(List<String> codes) {
        log.info("StockRelationService selectReuseCodeMap codes: {}", JSON.toJSONString(codes));
        List<StockDefine> dbDefines = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery().in(StockDefine::getCode, codes));
        List<String> dbCodes = CollUtil.map(dbDefines, StockDefine::getCode, true);

        if (CollUtil.isEmpty(dbCodes)) {
            return Collections.emptyMap();
        }
        List<HkStockRelation> hkStockRelations = stockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, dbCodes));
        Map<String, List<String>> sourceInnerMap = hkStockRelations.stream().collect(Collectors.groupingBy(HkStockRelation::getSourceCode, Collectors.mapping(HkStockRelation::getInnerCode, Collectors.toList())));

        return getMaxInnerCodeMap(false, sourceInnerMap);
    }

    /**
     * 根据股票code(原code/现code)查询股票关联关系
     *
     * @param sourceCodes 原股票code
     * @return
     */
    @Override
    public List<HkStockRelation> selectByCodes(List<String> sourceCodes) {
        return stockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, sourceCodes).or().in(HkStockRelation::getInnerCode, sourceCodes));
    }
    /**
     * 获取指定日期发生转板/代码复用的股票信息
     *
     * @param bizTypes 业务类型，2-代码复用，3-转板
     * @param bizTime 业务时间，格式：yyyyMMdd，如临时股票存并行交易结束时间，代码复用或转板存对应变更时间
     * @return
     */
    @Override
    public List<HkStockRelation> selectByBizTypeAndBizTime(List<Integer> bizTypes, String bizTime) {

        return stockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().eq(HkStockRelation::getBizTime, bizTime).in(HkStockRelation::getBizType, bizTypes));

    }

    public Long updateStockRelation(ComStockRelationDto relationDto) {
        log.info("StockRelationService updateStockRelation relationDto: {}", JSON.toJSONString(relationDto));
        Integer bizType = relationDto.getBizType();
        List<Integer> typeList = CollUtil.map(ListUtil.of(StockRelationBizEnum.TEMP, StockRelationBizEnum.REUSE, StockRelationBizEnum.TRANSFER, StockRelationBizEnum.SHAREHOLD), StockRelationBizEnum::getCode, true);

        if (!CollUtil.contains(typeList, bizType)) {
            log.info("StockRelationService updateStockRelation 不支持此场景！ bizType = {}", bizType);
            return null;
        }

        Assert.notNull(relationDto.getStockId(), "StockRelationService updateStockRelation failed, because the stockId for [{}] is null!", relationDto.getSourceCode());

        Long stockId = relationDto.getStockId();

        if (ObjectUtil.equal(StockRelationBizEnum.REUSE.getCode(), bizType)) {
            // 00700-10.hk
            relationDto.setInnerCode(getReuseInnerCode(relationDto.getSourceCode()));
            // 更新innerCode=00700.hk的记录
            Long sid = stockRelationMapper.selectStockIdByInnerCode(relationDto.getSourceCode());
            relationDto.setStockId(sid);
        }

        // 1. 更新t_hk_stock_relation表记录
        saveStockRelation(relationDto);

        if (ObjectUtil.equal(StockRelationBizEnum.TRANSFER.getCode(), bizType) || ObjectUtil.equal(StockRelationBizEnum.REUSE.getCode(), bizType)) {
            // 2. 更新t_stock_define
            if (ObjectUtil.equal(StockRelationBizEnum.TRANSFER.getCode(), relationDto.getBizType())) {
                // 如果转板后的code已存在，删除
                boolean flag = stockDefineService.remove(Wrappers.<StockDefine>lambdaQuery().eq(StockDefine::getCode, relationDto.getInnerCode()));
                log.info("StockRelationService saveStockRelation transfer delete define: {}, count: {}", relationDto.getInnerCode(), flag);
            }
            LambdaUpdateWrapper<StockDefine> wrapper = Wrappers.<StockDefine>lambdaUpdate().set(StockDefine::getCode, relationDto.getInnerCode()).eq(StockDefine::getCode, relationDto.getSourceCode());
            stockDefineService.update(wrapper);

            // 复用，生成一条新关联关系并更新基础信息
            ComStockRelationDto newRelation = ComStockRelationDto.builder().stockId(null).innerCode(relationDto.getInnerCode())
                    .sourceCode(relationDto.getSourceCode()).bizType(relationDto.getBizType()).bizTime(relationDto.getBizTime()).remark(relationDto.getRemark()).build();
            stockId = this.buildStockId(newRelation);
        }

        return stockId;
    }

    public Long saveStockDefineAndRelation(ComStockRelationDto relationDto) {
        log.info("StockRelationService saveStockDefineAndRelation relationDto: {}", JSON.toJSONString(relationDto));
        // 复用后的股票code
        String reuseCode = relationDto.getInnerCode();
        // 如果是复用，查询复用前code对应stockId
        String innerCode = ObjectUtil.equal(StockRelationBizEnum.REUSE.getCode(), relationDto.getBizType()) ? relationDto.getSourceCode() : relationDto.getInnerCode();
        Integer bizType = relationDto.getBizType();
        Long stockId = stockRelationMapper.selectStockIdByInnerCode(innerCode);
        if (ObjectUtil.isEmpty(stockId)) {
            if (ObjectUtil.equal(StockRelationBizEnum.REUSE.getCode(), relationDto.getBizType())) {
                // sourceCode: 00700.hk, innerCode: 00700-1.hk
                relationDto.setInnerCode(relationDto.getSourceCode());
            }
            // 1. 保存t_hk_stock_relation表记录
            stockId = saveStockRelation(relationDto);
            log.info("StockRelationService saveStockDefineAndRelation stockId: {}", stockId);

            // 新股和复用，都保存到码表； 同步码表，不做操作
            if (!ObjectUtil.equals(StockRelationBizEnum.SYNC.getCode(), bizType)) {
                // 查询code是否有码表记录
                StockDefine dbDefine = stockDefineService.lambdaQuery().eq(StockDefine::getCode, innerCode).last("limit 1").one();
                // 2. 保存t_stock_define
                if (ObjectUtil.isEmpty(dbDefine) && (ObjectUtil.equal(StockRelationBizEnum.NEW.getCode(), bizType)
                        || ObjectUtil.equal(StockRelationBizEnum.TEMP.getCode(), bizType) || ObjectUtil.equal(StockRelationBizEnum.SHAREHOLD.getCode(), bizType))) {
                    StockDefine stockDefine = new StockDefine();
                    stockDefine.setCode(innerCode);
                    stockDefineService.save(stockDefine);
                } else if (ObjectUtil.equal(StockRelationBizEnum.REUSE.getCode(), bizType) && ObjectUtil.isEmpty(dbDefine)) {
                    StockDefine oldDefine = stockDefineService.getOne(Wrappers.<StockDefine>lambdaQuery().eq(StockDefine::getCode, reuseCode).last("limit 1"));
                    if (ObjectUtil.isNotEmpty(oldDefine)) {
                        StockDefine reuseDefine = BeanUtil.copyProperties(oldDefine, StockDefine.class);
                        reuseDefine.setId(null);
                        reuseDefine.setCode(innerCode);
                        stockDefineService.save(reuseDefine);
                    }
                }
            }
        }
        return stockId;
    }

    private Long saveStockRelation(ComStockRelationDto relationDto) {
        log.info("StockRelationService saveStockRelation relationDto: {}", JSON.toJSONString(relationDto));
        Long stockId = relationDto.getStockId();
        HkStockRelation relation = new HkStockRelation();
        relation.setBizTime(relationDto.getBizTime());
        relation.setInnerCode(relationDto.getInnerCode());
        if (ObjectUtil.isEmpty(stockId)) {
            stockId = StockIdUtil.getHkStockId(stockRelationMapper.selectMaxStockId());
            relation.setStockId(stockId);
            // 新股： 业务类型为0
            relation.setBizType(StockRelationBizEnum.NORMAL.getCode());
            relation.setSecurityStatus(ObjectUtil.defaultIfNull(relationDto.getSecurityStatus(), StockRelationStatusEnum.NORMAL.getCode()));
            relation.setSourceCode(StrUtil.blankToDefault(relationDto.getSourceCode(), relationDto.getInnerCode()));
            relation.setRemark(relationDto.getRemark());
            stockRelationMapper.insert(relation);
        } else {
            relation.setStockId(stockId);
            relation.setBizType(relationDto.getBizType());
            relation.setSecurityStatus(relationDto.getSecurityStatus());
            relation.setSourceCode(relationDto.getSourceCode());
            relation.setUpdateTime(LocalDateTime.now());
            relation.setRemark(getSaveRemark(relation.getRemark(), relationDto.getRemark()));
            boolean isAllEmpty = ObjectUtil.isAllEmpty(relation.getSourceCode(), relation.getInnerCode(), relation.getBizType(), relation.getBizTime(), relation.getSecurityStatus(), relation.getRemark());
            if (!isAllEmpty) {
                if (ObjectUtil.equal(StockRelationBizEnum.TRANSFER.getCode(), relationDto.getBizType())) {
                    // 如果转板后的code已存在，删除
                    int count = stockRelationMapper.delete(Wrappers.<HkStockRelation>lambdaQuery().eq(HkStockRelation::getInnerCode, relationDto.getInnerCode()));
                    log.info("StockRelationService saveStockRelation transfer delete relation: {}, count: {}", relationDto.getInnerCode(), count);
                }
                stockRelationMapper.updateSelectiveByStockId(relation);
            }
        }

        return stockId;
    }

    private void updateStockInfo(String code) {
        String thName = Thread.currentThread().getName();
        // 异步操作
        ThreadUtil.execAsync(() -> {
            log.info("StockRelationService updateStockInfo code: {}", code);
            // 如果码表已存在不执行
            List<StockDefine> dbDefines = stockDefineService.listDefinesByCodeType(ListUtil.of(code), null);
            if (CollUtil.isEmpty(dbDefines)) {
                log.info("StockRelationService updateStockInfo not exist, code: {}", code);
                return;
            }

            StockDefine define = CollUtil.getFirst(dbDefines);
            boolean isAllEmpty = ObjectUtil.isAllEmpty(define.getName(), define.getStockName(), define.getStockType(), define.getProducttype());
            if (!isAllEmpty) {
                log.info("StockRelationService updateStockInfo return, code: {}", code);
//                return;
            }

            // 1. 调用sdk同步静态信息
            List<StockDefine> stockDefines = stockInfoApi.getAllStockDefines(ListUtil.of(code));
            log.info("StockRelationService updateStockInfo stockDefines: {}", CollUtil.size(stockDefines));
            if (CollUtil.isNotEmpty(stockDefines)) {
                stockInfoApi.saveStockInfo(false, stockDefines);
                // 2. 刷新缓存
                // stockCache.updateStockSimpleInfo();
                stockInfoApi.sendUpdateMessage();
            }
        });
    }

    private String getReuseInnerCode(String sourceCode) {
        log.info("StockRelationService getNewInnerCode sourceCode: {}", sourceCode);
        Lock lock = redisLockRegistry.obtain(RedisKeyConstants.BG_STOCK_RELATION_CODE_LOCK + sourceCode);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (locked) {
                // 如果是代码复用，可以复用多次， 生成code 00700-1.hk ~ 00700-10.hk
                List<String> innerCodeList = stockRelationMapper.selectInnerCodeList(sourceCode);
                Map<String, List<String>> sourceInnerMap = MapUtil.builder(sourceCode, innerCodeList).build();
                return this.getMaxInnerCodeMap(true, sourceInnerMap).get(sourceCode);
            }
        } catch (InterruptedException e) {
            log.error("StockRelationService getNewInnerCode occurs error", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        return sourceCode;
    }

    private Map<String, String> getMaxInnerCodeMap(boolean getNew, Map<String, List<String>> sourceInnerMap) {
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : sourceInnerMap.entrySet()) {
            String sourceCode = entry.getKey();
            List<String> innerCodeList = entry.getValue();
            // 00700
            String left = StrUtil.subBefore(sourceCode, ".", true);
            // 00700-1, 00700-2
            List<Integer> seqList = CollUtil.map(innerCodeList, inner -> {
                String suffix = StrUtil.subAfter(StrUtil.subBefore(inner, ".", true), "-", true);
                return StrUtil.isNotBlank(suffix) && StrUtil.isNumeric(suffix) ? Integer.parseInt(suffix) : 0;
            }, true);
            // 最大序号
            Integer max = CollUtil.max(seqList);
            // 如果要生成新的, 序号递增； 否则返回最大序号
            String index = String.valueOf(getNew ? max + 1 : max);
            // 新code
            String newInnerCode = StrUtil.concat(false, left, "-", index, ".hk");
            resultMap.put(sourceCode, newInnerCode);
        }
        log.info("StockRelationService getMaxInnerCodeMap resultMap: {}", JSON.toJSONString(resultMap));
        return resultMap;
    }

    private String getSaveRemark(String oldRemark, String newRemark) {
        String remark = oldRemark + "; " + newRemark;
        // 如果过长，进行截断
        return StrUtil.sub(remark, 0, 512);
    }
}




