package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenlibao.security.sdk.ws.core.model.req.StockSearchReq;
import com.fenlibao.security.sdk.ws.core.model.resp.StockSearchResp;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.GlobalConstants;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.SecurityType;
import com.vv.finance.common.dto.ComXnhk0127Dto;
import com.vv.finance.common.dto.QueryComSimpleStockDefineDto;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.ComStockRelationDto;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.PreAdjPriceInfo;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import com.vv.finance.common.entity.quotation.StockDefinePageReq;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.enums.*;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.JsonUtils;
import com.vv.finance.common.utils.KlineCalcUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.f10.F10PageBaseReq;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.entity.f10.shareholder.EquityChange;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0005Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.dto.SuspensionDto;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.HkStockRelationService;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import com.vv.finance.investment.bg.util.OperationEventUtil;
import com.vv.finance.investment.bg.utils.parser.MapstructParser;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import com.vv.finance.investment.gateway.dto.req.HKStockInfoReq;
import com.vv.finance.investment.gateway.dto.req.HkDrmbReq;
import com.vv.finance.investment.gateway.dto.req.HkIndustryInfoReq;
import com.vv.finance.investment.gateway.dto.req.HkQuotesReq;
import com.vv.finance.investment.gateway.dto.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.vv.finance.investment.bg.stock.info.StockDefine.COL_INDUSTRY_CODE;

/**
 * @author hamilton
 * @date 2020/10/27 18:02
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class StockInfoApiImpl implements StockInfoApi {
    @Autowired
    private IStockDefineService stockDefineService;

    @Autowired
    private IStockNewsService stockNewsService;

    CompletionService<StockDefine> completionService;


    @Value("#{'${hk.hot.stock}'.split(',')}")
    private List<String> stocks;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    StockCache stockCache;

    @Autowired
    Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    OperationEventUtil operationEventUtil;

    @Resource
    IStockMarketService stockMarketService;

    @Resource
    UtsInfoService utsInfoService;

    @Resource
    private Xnhk0005Mapper xnhk0005Mapper;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private IStockBusinessApi stockBusinessApi;
    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    private static final String TEST_FLAG = "0";
    // private static final String TEST_FLAG = "0";

    private static final String TRADE_STOCK_KEY = " BG:STOCK_INFO:TRADE:CODE";

    private static final String INDUSTRY_STOCK_KEY = " BG:STOCK_INDUSTRY:ALL:CODE";

    @Value("${industry.save.time:0640}")
    private String industrySaveTime;

    @Resource
    private HkStockRelationService hkStockRelationService;

    @Resource
    private HkStockRelationMapper stockRelationMapper;

    @Resource
    private RedisTemplate redisTemplate;
    @Value("${stock.name.topic:stock_name_topic}")
    private String stockTopic;
    @Override
    public ResultT<Void> saveStockInfo(boolean saveAll, List<StockDefine> stockDefineList) {
        // List<StockDefine> collect = stockDefineList.stream().filter(stockDefine -> TEST_FLAG.equals(stockDefine.getTestflag())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stockDefineList)) {
            return ResultT.success();
        }
        // 数据库码表
        List<StockDefine> stockOlds = stockDefineService.list();
        List<String[]> changeNameList = new ArrayList<>(stockOlds.size());
        // 退市股票
        List<String> quitCodes = stockMarketService.getQuitCode().getData();
        Map<String, StockDefine> dbDefineMap = stockOlds.stream().collect(Collectors.toMap(StockDefine::getCode, stockDefine -> stockDefine, (k1, k2) -> k1));
        Map<String, StockDefine> sdkDefineMap = stockDefineList.stream().collect(Collectors.toMap(StockDefine::getCode, stockDefine -> stockDefine, (k1, k2) -> k1));
        Set<String> dbCodes = dbDefineMap.keySet();
        Set<String> sdkCodes = sdkDefineMap.keySet();
        Set<String> codeAll = saveAll ? CollUtil.unionDistinct(dbCodes, sdkCodes) : sdkCodes;
        //查询股票所属行业
        Map<String, String> replenishIndustryMap = getReplenishIndustryMap(codeAll);
        List<StockDefine> updateList = new ArrayList<>(CollUtil.size(codeAll));
        //处理数据
        codeAll.forEach(code -> {
            String industryCode = null;
            StockDefine stockDefine = null;
            if (MapUtils.isNotEmpty(replenishIndustryMap)) {
                industryCode = replenishIndustryMap.get(code);
            }

            if (!dbCodes.contains(code)) {
                // 新股，数据库中不存在
                stockDefine = sdkDefineMap.get(code);
                stockDefine.setStockName(getStockName(stockDefine.getShortname(), stockDefine.getName()));
                stockDefine.setUpdateStockNameTime(LocalDateTime.now());
                stockDefine.setUpdateUserId(GlobalConstants.DEFAULT_SYSTEM_USER_ID);
                //行业code
                // stockDefine.setIndustryCode(StringUtils.isNotEmpty(industryCode) ? industryCode : stockDefine.getIndustryCode());
                // stockDefineService.save(stockDefine);
            } else if (!sdkCodes.contains(code)) {
//                // 历史股票，最新sdk中未同步
                stockDefine = dbDefineMap.get(code);
                stockDefine.setStockName(getStockName(stockDefine.getShortname(), stockDefine.getName()));
            } else {
                stockDefine = sdkDefineMap.get(code);
                StockDefine stockOld = dbDefineMap.get(code);
                stockDefine.setId(stockOld.getId());
                String newStockName = getStockName(stockDefine.getShortname(), stockDefine.getName());
                String oldStockName = getStockName(stockOld.getShortname(), stockOld.getName());
                log.info("oldStockName:{} newStockName:{} name:{} shortName:{}", stockOld.getStockName(), newStockName, stockDefine.getName(), stockDefine.getShortname());
                if (StrUtil.isNotBlank(newStockName) && !Objects.equals(oldStockName, newStockName)) {
                    // 股票名称变更，记录变更时间
                    stockDefine.setStockName(newStockName);
                    stockDefine.setUpdateUserId(GlobalConstants.DEFAULT_SYSTEM_USER_ID);
                    stockDefine.setUpdateStockNameTime(LocalDateTime.now());
                    log.info("stockName有变更，stockOld.getStockName():{} oldStockName:{} newStockName:{} name:{} shortName:{}", stockOld.getStockName(), oldStockName, newStockName, stockDefine.getName(), stockDefine.getShortname());
                    changeNameList.add(ListUtil.toList(stockDefine.getCode(), stockOld.getStockName(), stockDefine.getStockName()).toArray(new String[]{}));
                } else {
                    // 否则股票名称用历史的
                    stockDefine.setStockName(stockOld.getStockName());
                    stockDefine.setUpdateUserId(stockOld.getUpdateUserId());
                    stockDefine.setUpdateStockNameTime(stockOld.getUpdateStockNameTime());
                }

                // 市场代号
                stockDefine.setMarketcode(StrUtil.blankToDefault(stockDefine.getMarketcode(), stockOld.getMarketcode()));
                // 币种
                stockDefine.setCurrency(StrUtil.blankToDefault(stockDefine.getCurrency(), stockOld.getCurrency()));
                // isin
                stockDefine.setIsincode(StrUtil.blankToDefault(stockDefine.getIsincode(), stockOld.getIsincode()));
                // 股票类型
                stockDefine.setStockType(ObjectUtil.defaultIfNull(stockDefine.getStockType(), stockOld.getStockType()));
                // 证券类型
                stockDefine.setProducttype(ObjectUtil.defaultIfNull(stockDefine.getProducttype(), stockOld.getProducttype()));
                // 沽空标识
                stockDefine.setShortsell(StrUtil.blankToDefault(stockDefine.getShortsell(), stockOld.getShortsell()));
                // 上市日期
                stockDefine.setListingdate(StrUtil.blankToDefault(stockDefine.getListingdate(), stockOld.getListingdate()));
                // 退市日期
                stockDefine.setDelistingdate(StrUtil.blankToDefault(stockDefine.getDelistingdate(), stockOld.getDelistingdate()));
                // 状态
                stockDefine.setSuspension(ObjectUtil.defaultIfNull(stockDefine.getSuspension(), stockOld.getSuspension()));
            }
            //行业code
            if (ObjectUtil.equal(StockTypeEnum.STOCK.getCode(), stockDefine.getStockType())) {
                stockDefine.setIndustryCode(StringUtils.isNotEmpty(industryCode) ? industryCode : stockDefine.getIndustryCode());
            } else {
                // 非正股，不设置行业
                stockDefine.setIndustryCode(null);
            }

            if (StrUtil.isBlank(stockDefine.getStockName())) {
                stockDefine.setStockName(stockDefine.getCode());
            }

            // 设置默认股票类型
            stockDefine.setStockType(ObjectUtil.defaultIfNull(stockDefine.getStockType(), StockTypeEnum.STOCK.getCode()));

            // 退市股票
            // 20240621: 股票变成退市之后，证券列表中不存在了
            stockDefine.setSuspension(quitCodes.contains(stockDefine.getCode()) ? StockSecurityStatusEnum.QUIT.getCode() : stockDefine.getSuspension());
            // 2024801: 如果状态为null，设置为退市
            stockDefine.setSuspension(ObjectUtil.defaultIfNull(stockDefine.getSuspension(), StockSecurityStatusEnum.QUIT.getCode()));
            //打印日志
            log.info("更新码表信息,code:{},数据:{}", stockDefine.getCode(), JSON.toJSONString(stockDefine));
            updateList.add(stockDefine);
        });

        if (CollUtil.isNotEmpty(updateList)) {
            stockDefineMapper.batchSaveOrUpdate(updateList);
        }

        // 查询没有成分股的行业(成分股是正股)
        List<Long> idList = stockDefineMapper.selectNoCompStocks();
        if (CollUtil.isNotEmpty(idList)) {
            stockDefineMapper.deleteBatchIds(idList);
        }

        if (CollUtil.isNotEmpty(changeNameList)) {
            log.info("StockInfoApi saveStockInfo changeNameList is {}", Arrays.toString(changeNameList.toArray()));
            for (String[] params : changeNameList) {
                operationEventUtil.sendDingDingMessage(params);
            }
        }

        return ResultT.success();
    }
    //筛出无行业code的股票，并去uts数据库查询这些股票的行业code以作补偿
    private Map<String, String> getReplenishIndustryMap(Set<String> codes) {
        Map<String, String> replenishIndustryMap = Maps.newHashMap();
        //获取行业code的股票
        if (CollUtil.isNotEmpty(codes)) {
            List<Xnhks0101> xnhks0101s = xnhks0101Mapper.selectList(new QueryWrapper<Xnhks0101>().in(Xnhks0101.SECCODE, codes));
            if (CollUtil.isNotEmpty(xnhks0101s)) {
                replenishIndustryMap = xnhks0101s.stream().filter(k1 -> StringUtils.isNotBlank(k1.getF017v())).collect(Collectors.toMap(Xnhks0101::getSeccode, f -> StrUtil.concat(false, "HY", f.getF017v(), ".hk"), (k1, k2) -> k1));
            }
        }
        return replenishIndustryMap;
    }

    @Override
    // @Transactional(rollbackFor = Exception.class)
    // @CacheEvict(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY, key = "'stock:'+#stockDefine.code")
    public ResultT<Void> saveStockInfo(StockDefine stockDefine) {
        // if (TEST_FLAG.equals(stockDefine.getTestflag())) {
        //     return ResultT.success();
        // }
        stockDefineService.saveOrUpdate(stockDefine, new UpdateWrapper<StockDefine>().eq("code", stockDefine.getCode()));
        this.sendUpdateMessage();
        // stockCache.updateStockSimpleInfo();
        return ResultT.success();
    }

    @Override
    public ResultT<Void> sendUpdateMessage() {
        redisTemplate.convertAndSend(stockTopic, "刷新本地缓存: " + MDC.get(GlobalConstants.TRACE_ID));
        return ResultT.success();
    }

    @Override
    // @CacheEvict(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY, key = "'stock:'+#suspensionDto.code")
    public ResultT<Void> updateSuspension(SuspensionDto suspensionDto) {
        StockDefine stockDefine = new StockDefine();
        Integer securityStatus = StateSecurityEnum.getSecurityBySuspension(Integer.parseInt(suspensionDto.getSuspension()));
        stockDefine.setSuspension(securityStatus);
        stockDefineService.update(stockDefine, new UpdateWrapper<StockDefine>().eq("code", suspensionDto.getCode()));
        return ResultT.success();
    }


    @Override
    // @Cacheable(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY, key = "'stock:'+#code")
    public ResultT<StockDefine> queryStockDefine(String code) {
        return ResultT.success(stockDefineService.getOne(new QueryWrapper<StockDefine>().eq("code", code)));
    }

    @Override
    public ResultT<List<StockDefine>> queryStockListByIndustry(String industryCode) {
        return ResultT.success(stockDefineService.list(new QueryWrapper<StockDefine>().eq(COL_INDUSTRY_CODE, industryCode).eq("stock_type", StockTypeEnum.STOCK.getCode())));
    }

    @Override
    public ResultT<Page<StockDefine>> listStockDefine(Integer size, Integer pageNum, String market) {
        if (size == null || size == 0 || pageNum == null || pageNum == 0) {
            List<StockDefine> list = stockDefineService.listStockColumns(null);
            Page<StockDefine> page = new Page<>();
            page.setRecords(list);
            page.setCurrent(1L);
            page.setSize(list.size());
            return ResultT.success(page);
        }
        Page<StockDefine> market1 = stockDefineService.page(new Page<>(pageNum, size));

        return ResultT.success(market1);
    }

    @Override
    public ResultT<PageDomain<StockDefine>> pageStockDefine(StockDefinePageReq req) {
        Page<StockDefine> page = new Page<>();
        page.setCurrent(req.getCurrentPage());
        page.setSize(req.getPageSize());
        Page<StockDefine> maStockDefinePage = stockDefineMapper.pageStockDefine(page, req);
        PageDomain<StockDefine> result = new PageDomain<>();
        BeanUtils.copyProperties(maStockDefinePage, result);
        return ResultT.success(result);
    }

    @Override
    public ResultT<PageDomain<StockDefine>> pageStockDefine(F10PageBaseReq f10PageReq) {
        Page<StockDefine> maStockDefinePage = stockDefineService.page(new Page<>(f10PageReq.getCurrentPage(), f10PageReq.getPageSize())
                , new QueryWrapper<StockDefine>().like(StringUtils.isNotEmpty(f10PageReq.getStockCode()),
                        "code", f10PageReq.getStockCode()).eq("stock_type", StockTypeEnum.STOCK.getCode()));
        PageDomain<StockDefine> result = new PageDomain<>();
        BeanUtils.copyProperties(maStockDefinePage, result);
        return ResultT.success(result);
    }



    @Override
    public Map<String, String> stockRelationIndustryCode() {
        // List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>().select("code", "industry_code"));
        List<StockDefine> list = stockDefineService.listStockColumns(ListUtil.of("code", "industry_code"));
        return list.stream().filter(stockDefine ->
                StringUtils.isNotEmpty(stockDefine.getIndustryCode())).
                collect(Collectors.toMap(StockDefine::getCode, StockDefine::getIndustryCode));

    }

    @Override
    public ResultT<List<String>> allStockDefineCodes() {
        List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>()
                .ne("suspension", 3).eq("stock_type", StockTypeEnum.STOCK.getCode())
                .eq("instrument", "EQTY").select("code"));
        List<String> codes = list.stream().map(StockDefine::getCode).collect(Collectors.toList());
        return ResultT.success(codes);
    }

    @Override
    public ResultT<List<StockDefine>> getStockDefineByStockType(Integer stockType) {
        List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>()
                .eq("stock_type", stockType));
        return ResultT.success(list);
    }

    @Override
    public List<String> stockCodes() {
        List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>().select("code").eq("instrument", "EQTY"));
        return list.stream().map(StockDefine::getCode).collect(Collectors.toList());
    }

    @Override
    public List<Object> allStockCodes() {
        return stockDefineService.listObjs(new QueryWrapper<StockDefine>().select("code"));
    }

    @Override
    public List<String> tradeStockCodes() {
        try {

            List<String> collect = stockSearchReq(SecurityType.EQTY).stream().map(StockSearchResp::getSymbol).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(collect)) {
                return collect;
            }
            return (List<String>) redisClient.get(TRADE_STOCK_KEY);

        } catch (Exception e) {

            return (List<String>) redisClient.get(TRADE_STOCK_KEY);
        }

    }

    @Override
    public ResultT<List<String>> getStockCodeBySuspension(Integer suspension) {
        List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>()
                .ne(suspension != null, "suspension", suspension)
                .eq("instrument", "EQTY").select("code"));
        List<String> codes = list.stream().map(StockDefine::getCode).collect(Collectors.toList());
        return ResultT.success(codes);
    }


    @Override
    // @CacheEvict(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY, allEntries = true)
    public void initStockDefine() {
        log.info("initStockDefine begin");
        List<StockSearchResp> list = stockSearchReq(SecurityType.EQTY);
        //TODO-luoyj 为什么list为空、sleep2秒之后再查？
        if (CollUtil.isEmpty(list)) {
            try {
                Thread.sleep(2000);
                list = stockSearchReq(SecurityType.EQTY);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
        List<String> collect = list.stream().map(StockSearchResp::getSymbol).collect(Collectors.toList());

        redisClient.set(TRADE_STOCK_KEY, collect);


        if (list.isEmpty()) {
            return;
        }

        List<StockDefine> stockDefineList = Lists.newLinkedList();

        List<String> codeList = list.stream().map(StockSearchResp::getSymbol).collect(Collectors.toList());

        // 20240621:  v3/stock/list  只有当日交易股票（包含新股）
        // 非正在交易股票，基本信息可能会有变更，也需要进行同步
        List<StockDefine> dbDefines = stockDefineService.listStockColumns(ListUtil.of("code"));
        List<String> dbCodes = CollUtil.map(dbDefines, StockDefine::getCode, true);
        Set<String> codeAll = CollUtil.unionDistinct(codeList, dbCodes);

        List<StockDefine> allStockDefines = getAllStockDefines(new ArrayList<>(codeAll));
        stockDefineList.addAll(allStockDefines);

//        if (completionService == null) {
//            completionService = new ExecutorCompletionService<StockDefine>
//                    (new ThreadPoolExecutor(8, 8, 0,
//                            TimeUnit.SECONDS, new LinkedBlockingQueue<>(20000),
//                            new CustomizableThreadFactory("initStockDefine")));
//        }
//        CountDownLatch count = new CountDownLatch(list.size());
//        list.forEach(stockSearchResp -> completionService.submit(new StockWorker(stockSearchResp.getSymbol(), count)));
//        try {
//            count.await();
//        } catch (InterruptedException e) {
//            log.error("completionService initStockDefine error", e);
//        }
//        List<StockDefine> stockDefineList = Lists.newLinkedList();
//        for (int i = 0; i < list.size(); i++) {
//            try {
//                Future<StockDefine> take = completionService.take();
//                StockDefine stockDefine = take.get();
//                if (stockDefine != null) {
//                    stockDefineList.add(stockDefine);
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                log.error("completionService initStockDefine error", e);
//
//            }
//        }
        // 查询所有行业
        try {
            // List<StockDefine> industryDefines = xnhk0005Mapper.listIndustryDefines();
            HkIndustryInfoReq industryReq = HkIndustryInfoReq.builder().conceptFlag("N").build();
            List<HkIndustryResp> industryRespList = stockBusinessApi.getIndustryList(industryReq).getData();
            if (CollUtil.isNotEmpty(industryRespList)) {
                redisClient.set(INDUSTRY_STOCK_KEY, industryRespList);
                List<StockDefine> industryDefines = industryRespList.stream().map(resp -> {
                    StockDefine stockDefine = new StockDefine();
                    stockDefine.setCode(resp.getSymbol());
                    stockDefine.setName(resp.getName());
                    stockDefine.setStockName(resp.getName());
                    stockDefine.setProducttype("1");
                    stockDefine.setStockType(12);
                    stockDefine.setSuspension(StockSecurityStatusEnum.NORMAL.getCode());
                    return stockDefine;
                }).collect(Collectors.toList());

                // 码表同步6点40不新增行业
                LocalTime saveTime = LocalDateTimeUtil.parse(industrySaveTime, "HHmm").toLocalTime();
                if (LocalTime.now().isAfter(saveTime)) {
                    // 数据库行业
                    List<StockDefine> dbHyDefines = stockDefineService.listColumnsByType(null, StockTypeEnum.HY.getCode());
                    // 行业code
                    List<String> dbHyCodes = CollUtil.map(dbHyDefines, StockDefine::getCode, true);
                    List<String> sdkHyCodes = CollUtil.map(industryDefines, StockDefine::getCode, true);
                    // 新增行业
                    Collection<String> newHyCodes = CollUtil.subtract(sdkHyCodes, dbHyCodes);
                    if (CollUtil.isNotEmpty(newHyCodes)) {
                        log.info("initStockDefine超过6点40不落库行业 {}", Arrays.toString(newHyCodes.toArray()));
                        // 过滤掉不落库行业
                        industryDefines = CollUtil.filter(industryDefines, id -> !newHyCodes.contains(id.getCode()));
                    }
                }

                stockDefineList.addAll(industryDefines);
            }
        } catch (Exception e) {
            log.error("stockInfoApi initStockDefine occurs error", e);
        }

        // 查询双柜台股票列表
        try {
            HkDrmbReq hkDrmbReq = new HkDrmbReq();
            hkDrmbReq.setDate(DateUtils.formatDate(new Date()));
            log.info("调用融聚汇获取双柜台股票关联代码列表请求参数：{}", JsonUtils.beanToJson(hkDrmbReq));
            HkDrmbResp rsp = stockBusinessApi.getStockHkdrmblist(hkDrmbReq);
            log.info("调用融聚汇获取双柜台股票关联代码列表响应结果：{}", JsonUtils.beanToJson(rsp));
            if (StringUtils.equals(rsp.getCode(), "200") && ObjectUtil.isNotEmpty(rsp.getBody())) {
                HkDrmbDetailResp body = rsp.getBody();
                if (Objects.nonNull(body) && CollUtil.isNotEmpty(body.getVoList())) {
                    List<HkDrmbVoResp> voList = body.getVoList();
                    for (HkDrmbVoResp drmbRsp:voList) {
                        for (StockDefine stockDefine:stockDefineList) {
                            if(StringUtils.equals(drmbRsp.getHkdSymbol(), stockDefine.getCode())){
                                stockDefine.setDomainCode(drmbRsp.getRmbSymbol());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("stockInfoApi get domain_code occurs error", e);
        }

        this.saveStockInfo(true, stockDefineList);
        // 保存股票关联关系
        List<String> updateCodes = CollUtil.map(stockDefineList, StockDefine::getCode, true);
        hkStockRelationService.saveNewRelations(new ArrayList<>(updateCodes));

        // 保存股票金融类型
        List<Xnhks0101> xnhks0101s = xnhks0101Mapper.selectList(Wrappers.<Xnhks0101>lambdaQuery().isNotNull(Xnhks0101::getF026v));
        if (CollUtil.isNotEmpty(xnhks0101s)) {
            Map<String, Integer> financeMap = CollUtil.toMap(xnhks0101s, new HashMap<>(), Xnhks0101::getSeccode, xn -> Integer.valueOf(xn.getF026v()));
            redisClient.strSetHashPipelined(RedisKeyConstants.STOCK_FINANCE_TYPE, financeMap);
        }
        // // 刷新缓存
        // stockCache.updateStockSimpleInfo();
        log.info("initStockDefine end,szie={}", stockDefineList.size());
    }

    @Override
    public List<StockDefine> getAllStockDefines(List<String> codeAll) {
        List<StockDefine> stockDefineList = Lists.newLinkedList();
        List<ReuseTempDTO> tradingTempStocks = utsInfoService.findTradingTempStockByTime(new Date());
        Map<String, ReuseTempDTO> tempTradeMap = tradingTempStocks.stream().collect(Collectors.toMap(tempDTO -> tempDTO.getCode(), Function.identity(), (k1, k2) -> k1));

        // 如下字段都不为空，才更新码表
        Predicate<HKStockInfoResp> predicate = resp -> ObjectUtil.isAllNotEmpty(resp.getSecurityStatus(), resp.getProduct_type(), getStockName(resp.getShort_name(), resp.getName()));

        List<List<String>> partitions = ListUtil.partition(new ArrayList<>(codeAll), 10);
        for (List<String> part : partitions) {
            try {
                Thread.sleep(100);
                List<HKStockInfoResp> sdkRespList = stockBusinessApi.getStockDefines(HKStockInfoReq.builder().symbols(part).build());
                List<HKStockInfoResp> stockDefineResps = CollUtil.defaultIfEmpty(sdkRespList, new ArrayList<>()).stream().filter(predicate).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(stockDefineResps)) {
                    continue;
                }
                for (HKStockInfoResp stockDefineResp : stockDefineResps) {
                    StockDefine stockDefine = new StockDefine();
                    BeanUtils.copyProperties(stockDefineResp, stockDefine);
                    stockDefine.setLotsize(stockDefineResp.getLot_size());
                    stockDefine.setName(stockDefineResp.getName());
                    stockDefine.setCode(stockDefineResp.getSymbol());
                    stockDefine.setShortname(stockDefineResp.getShort_name());
                    stockDefine.setMarketcode(stockDefineResp.getMarket_code());
                    stockDefine.setIsincode(stockDefineResp.getIsin_code());
                    stockDefine.setProducttype(stockDefineResp.getProduct_type() + "");
                    // 如果sdk没有返回类型，默认设置为正股
                    stockDefine.setStockType(stockDefineResp.getProduct_type());
                    stockDefine.setPreclose(stockDefineResp.getPre_close());
                    stockDefine.setVcmflag(stockDefineResp.getVcm_flag());
                    stockDefine.setShortsell(stockDefineResp.getShort_sell());
                    stockDefine.setCasflag(stockDefineResp.getCas_flag());
                    stockDefine.setCcassflag(stockDefineResp.getCcas_flag());
                    stockDefine.setDummyflag(stockDefineResp.getDummy_flag());
                    stockDefine.setTestflag("0");
                    stockDefine.setStampdutyflag(stockDefineResp.getStampduty_flag());
                    stockDefine.setListingdate(stockDefineResp.getListing_date());
                    stockDefine.setDelistingdate(stockDefineResp.getDelisting_date());
                    stockDefine.setFreetext(stockDefineResp.getFree_text());
                    stockDefine.setEnfflag(stockDefineResp.getEnf_flag());
                    stockDefine.setCouponrate(stockDefineResp.getCoupon_rate());
                    stockDefine.setCounversionratio(StringUtils.isNotEmpty(stockDefineResp.getCounversion_ratio()) ? new BigDecimal(stockDefineResp.getCounversion_ratio()) : BigDecimal.ZERO);
                    stockDefine.setStrikeprice1(StringUtils.isNotEmpty(stockDefineResp.getStrike_price_1()) ? new BigDecimal(stockDefineResp.getStrike_price_1()) : BigDecimal.ZERO);
                    stockDefine.setStrikeprice2(StringUtils.isNotEmpty(stockDefineResp.getStrike_price_2()) ? new BigDecimal(stockDefineResp.getStrike_price_2()) : BigDecimal.ZERO);
                    stockDefine.setMaturitydate(stockDefineResp.getMaturity_date());
                    stockDefine.setCallput(stockDefineResp.getCall_put());
                    stockDefine.setWarrenttype(stockDefineResp.getWarrent_type());
                    stockDefine.setCallprice(stockDefineResp.getCall_price());
                    stockDefine.setDecimalprice(stockDefineResp.getDecimal_price());
                    stockDefine.setDecimalentitlement(stockDefineResp.getDecimal_entitlement());
                    stockDefine.setSuspension(stockDefineResp.getSecurityStatus());
                    //处于并行交易的临时股票上市日期处理
                    ReuseTempDTO reuseTempDTO = tempTradeMap.get(stockDefineResp.getSymbol());
                    if (reuseTempDTO != null) {
                        StockDefine relativeStockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", reuseTempDTO.getRelationCode()));
                        if (relativeStockDefine != null) {
                            stockDefine.setListingdate(relativeStockDefine.getListingdate());
                        }
                    }
                    stockDefineList.add(stockDefine);
                }
            } catch (Exception e) {
                log.error("获取股票详情", e);
            }
        }
        return stockDefineList;
    }

    @Override
    public ResultT<Map<String, String>> getAllStockListingDate() {
        List<StockDefine> stockDefines = stockDefineService.list();
        return ResultT.success(stockDefines.stream().collect(Collectors.toMap(StockDefine::getCode, StockDefine::getListingdate)));
    }

    @Override
    public ResultT<StockNewsEntity> getStockNewsById(Long stockNewsId) {
        StockNewsEntity stockNewsEntity = stockNewsService.getById(stockNewsId);
        return ResultT.success(stockNewsEntity);
    }


    private List<StockSearchResp> stockSearchReq(String type) {
        StockSearchReq stockSearchReq = StockSearchReq.builder()
                .searchDay(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .type(type)
                .build();

        try {
            PageInfo<StockSearchResp> pageInfo = stockBusinessApi.all(stockSearchReq).getData();
            return ObjectUtils.isEmpty(pageInfo) ? Collections.emptyList() : pageInfo.getList();
        } catch (Exception e) {
            log.error("stockInfoApi stockSearchReq Exception, req = [{}]", stockSearchReq, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, String> getStockCodeNameMap() {
        return stockCache.queryStockNameMap(null);
    }

    @Override
    public Map<String, ComSimpleStockDefine> getStockSimpleInfos() {
        List<ComStockSimpleDto> stockSimpleInfos = stockCache.queryStockInfoList(null);
        List<ComSimpleStockDefine> comSimpleStockDefines = MapstructParser.convertBean(stockSimpleInfos);
//        List<ComSimpleStockDefine> comSimpleStockDefines = BeanUtil.copyToList(stockSimpleInfos, ComSimpleStockDefine.class);
        Map<String, ComSimpleStockDefine> comSimpleStockDefineMap = comSimpleStockDefines.stream().collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        return comSimpleStockDefineMap;
    }

    @Override
    public Map<Long, ComSimpleStockDefine> getStockIdSimpleInfos() {
        List<ComStockSimpleDto> stockSimpleInfos = stockCache.queryStockInfoList(null);
        List<ComSimpleStockDefine> comSimpleStockDefines = MapstructParser.convertBean(stockSimpleInfos);
//        List<ComSimpleStockDefine> comSimpleStockDefines = BeanUtil.copyToList(stockSimpleInfos, ComSimpleStockDefine.class);
        Map<Long, ComSimpleStockDefine> comSimpleStockDefineMap = comSimpleStockDefines.stream().collect(Collectors.toMap(item -> item.getStockId(), Function.identity()));
        return comSimpleStockDefineMap;
    }

    @Override
    public ComSimpleStockDefine getStockSimpleInfoByStockId(Long stockId) {
//         ComSimpleStockDefine comSimpleStockDefine = null;
// //        stockCache.getStockSimpleInfoByCode()   // todo
//         Map<Long, ComSimpleStockDefine> comSimpleStockDefineMap = this.getStockIdSimpleInfos();
//         if (ObjectUtils.isNotEmpty(comSimpleStockDefineMap) && comSimpleStockDefineMap.get(stockId) != null) {
//             comSimpleStockDefine = comSimpleStockDefineMap.get(stockId);
//         }
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockDtoList(ListUtil.of(stockId));
        List<ComSimpleStockDefine> defineList = MapstructParser.convertBean(simpleDtoList);
        return CollUtil.getFirst(defineList);
    }
    /**
     * 根据查询条件从本地缓存中获取股票基本信息
     *
     */
    @Override
    public Map<String, ComSimpleStockDefine> getStockSimpleInfos(QueryComSimpleStockDefineDto query) {
        long l = System.currentTimeMillis();
        List<ComStockSimpleDto> stockSimpleInfos = stockCache.queryStockInfoList(null);
        stockSimpleInfos = stockSimpleInfos.stream().filter(o -> {
            if (ObjectUtils.isEmpty(query)) {
                return true;
            }
            String industryCode = query.getIndustryCode();
            Set<String> stockCodes = query.getStockCodes();
            Set<Integer> stockTypeSet = query.getStockTypeSet();
            Set<Integer> securityStatusSet = query.getSecurityStatusSet();
            Set<Long> stockIds = query.getStockIds();
            if ((StringUtils.isNotEmpty(industryCode) && !industryCode.equals(o.getIndustryCode())) ||
                    (CollUtil.isNotEmpty(stockTypeSet) && !stockTypeSet.contains(o.getStockType())) ||
                    (CollUtil.isNotEmpty(securityStatusSet) && !securityStatusSet.contains(o.getSecurityStatus())) ||
                    (CollUtil.isNotEmpty(stockCodes) && !stockCodes.contains(o.getCode())) ||
                    (CollUtil.isNotEmpty(stockIds) && !stockIds.contains(o.getStockId()))) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        List<ComSimpleStockDefine> comSimpleStockDefines = MapstructParser.convertBean(stockSimpleInfos);
//        List<ComSimpleStockDefine> comSimpleStockDefines = BeanUtil.copyToList(stockSimpleInfos, ComSimpleStockDefine.class);
        Map<String, ComSimpleStockDefine> comSimpleStockDefineMap = comSimpleStockDefines.stream().collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        return comSimpleStockDefineMap;
    }

    @Override
    public Set<String> getStockCodesByStockIds(Set<Long> stockIds) {
        QueryComSimpleStockDefineDto query = QueryComSimpleStockDefineDto.builder().stockIds(stockIds).build();
        Map<String, ComSimpleStockDefine> hkComSimpleStockDefineMap = getStockSimpleInfos(query);
        return hkComSimpleStockDefineMap.keySet();
    }

    @Override
    public List<ComStockSimpleDto> getStockSimpleInfoLists() {
        return stockCache.queryStockInfoList(null);
    }

    @Override
    public List<ComSimpleStockDefine> getStockInfoListByStockIds(List<Long> stockIds) {
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockDtoList(stockIds);
        return MapstructParser.convertBean(simpleDtoList);
    }

    @Override
    public ComSimpleStockDefine getStockSimpleInfo(String code) {
        ComSimpleStockDefine comSimpleStockDefine = new ComSimpleStockDefine();
        Map<String, ComSimpleStockDefine> comSimpleStockDefineMap = this.getStockSimpleInfos();
        if (ObjectUtils.isNotEmpty(comSimpleStockDefineMap) && comSimpleStockDefineMap.get(code) != null) {
            comSimpleStockDefine = comSimpleStockDefineMap.get(code);
        }
        return comSimpleStockDefine;
    }
    /**
     * 获取当前交易时段
     * @return
     */
    @Override
    public Integer getTradingPeriod() {
        return redisClient.get(RedisKeyConstants.RECEIVER_TRADING_SUSPENSION);
    }

    @Override
    public ResultT<Boolean> getBlockSnapshot() {
        List<String> todayBlockCodes = stockCache.getHYCodes();
        List<HKIndexQuoteResp> indexRespList = new ArrayList<>(CollUtil.size(todayBlockCodes));
        List<List<String>> partitions = ListUtil.partition(new ArrayList<>(todayBlockCodes), 10);

        for (List<String> part : partitions) {
            try {
                HkQuotesReq req = new HkQuotesReq();
                req.setSymbols(new ArrayList<>(part));
                req.setTimeMode(0);

                Thread.sleep(10);
                ResultT<List<HKIndexQuoteResp>> result = stockBusinessApi.indexQuoteV3(req);
                if (result.getCode() == ResultT.success().getCode() && CollUtil.isNotEmpty(result.getData())) {
                    //增加逻辑，判断行情时间为空说明没有行情
                    indexRespList.addAll(CollUtil.filterNew(result.getData(), resp -> ObjectUtil.isNotEmpty(resp.getTime())));
                }
            } catch (InterruptedException e) {
                log.error("获取板块快照数据 异常, codes: {}", part, e);
            }
        }

        if (CollUtil.isNotEmpty(indexRespList)) {
            redisClient.strSetListPipelined(RedisKeyConstants.BG_RJH_BLOCK_SNAPSHOT_BEAN, indexRespList);
        }
        return ResultT.success();
    }

    /**
     * 判断code是否为行业或概念
     */
    @Override
    public Boolean industryFlag(String code) {
        List<ComStockSimpleDto> stockSimpleInfo = stockCache.queryStockInfoList(null);
        List<String> industryCodes = stockSimpleInfo.stream().filter(o -> StockTypeEnum.HY.getCode().equals(o.getStockType())).map(o -> o.getCode()).collect(Collectors.toList());
        return industryCodes.contains(code);
    }

    @Override
    public ResultT<List<String>> listKlineIndustry() {
        List<HkIndustryResp> industryRespList = redisClient.get(INDUSTRY_STOCK_KEY);
        List<String> symbolList = CollUtil.map(industryRespList, HkIndustryResp::getSymbol, true);
        return ResultT.success(symbolList);
    }


    private String getStockName(String shortName, String name) {
        if (Validator.hasChinese(shortName)) {
            return shortName;
        }
        if (Validator.hasChinese(name)) {
            return name;
        }
        if (StrUtil.isNotBlank(shortName)) {
            return shortName;
        }
        return name;
    }

    /**
     * 计算前复权价格
     * @return
     */
    @Override
    public List<PreAdjPriceInfo> calcForwardPrice(List<PreAdjPriceInfo> preAdjPriceInfos) {
//        ResultT<List<BgTradingCalendar>> lastTradingCalendarsResult = hkTradingCalendarApi.getLastTradingCalendars(LocalDate.now(), 1);
//        List<BgTradingCalendar> lastTradingCalendars = lastTradingCalendarsResult.getData();
//        LocalDate tradingDate = lastTradingCalendars.get(0).getDate();
        LocalDate localDate = LocalDate.now();
        // 查询当天拆并股
        List<Xnhk0127> xnhk0127s = utsInfoService.getXnhk0127(DateUtil.date(localDate));
        if (CollUtil.isNotEmpty(xnhk0127s)) {
            List<ComXnhk0127Dto> comXnhk0127Dtos = BeanUtil.copyToList(xnhk0127s, ComXnhk0127Dto.class);
            Map<String, List<ComXnhk0127Dto>> xnhk0127Map = comXnhk0127Dtos.stream().collect(Collectors.groupingBy(ComXnhk0127Dto::getSeccode));
            preAdjPriceInfos.forEach(o -> {
                List<ComXnhk0127Dto> comXnhk0127List = xnhk0127Map.get(o.getStockCode());
                if (CollUtil.isNotEmpty(comXnhk0127List)) {
                    log.info("calcForwardPrice计算前复权价格 localDate:{} code：{}", localDate, o.getStockCode());

                    o.setPrice(KlineCalcUtils.hkCalcForwardAmount(o.getPrice(), comXnhk0127List, true));
                } else {
                    log.info("calcForwardPrice计算前复权价格 localDate:{} code：{} 无复权事件", localDate, o.getStockCode());
                }
            });
        } else {
            log.info("calcForwardPrice计算前复权价格 localDate:{}，无复权事件", localDate);
        }
        return preAdjPriceInfos;
    }

    @Override
    public Map<String, Long> selectStockIdAllByCodes(List<String> codes) {
        LambdaQueryWrapper<HkStockRelation> wrapper = Wrappers.<HkStockRelation>lambdaQuery().in(CollUtil.isNotEmpty(codes), HkStockRelation::getInnerCode, codes);
        List<HkStockRelation> stockRelations = hkStockRelationService.list(wrapper);
        // 状态为0
        Collection<HkStockRelation> validRelations = CollUtil.filterNew(stockRelations, sr -> ObjectUtil.equal(StockRelationStatusEnum.NORMAL.getCode(), sr.getSecurityStatus()));
        // 状态为1
        HashMap<String, Long> maxTimeIdMap = CollUtil.subtract(stockRelations, validRelations).stream().filter(s -> StrUtil.isNotBlank(s.getBizTime())).collect(
                Collectors.groupingBy(HkStockRelation::getInnerCode,
                        HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                list -> {
                                    HkStockRelation relation = CollUtil.getFirst(list.stream().sorted(Comparator.comparing(HkStockRelation::getBizTime).reversed()).collect(Collectors.toList()));
                                    return ObjectUtil.defaultIfNull(relation, HkStockRelation::getStockId, null);
                                }
                        )
                )
        );
        // 转为map
        Map<String, Long> stockIdMap = CollUtil.toMap(validRelations, new HashMap<>(4000), HkStockRelation::getInnerCode, HkStockRelation::getStockId);
        maxTimeIdMap.putAll(stockIdMap);
        return maxTimeIdMap;
    }

    @Override
    public Map<String, Long> selectStockIdMapByCodes(List<String> codes) {
        return hkStockRelationService.selectStockIdByCodes(codes);
    }

    @Override
    public Map<String, Long> selectStockIdByCodes(List<String> codes) {
        return stockCache.queryStockIdMap(codes);
    }

    @Override
    public Map<String, Long> updateStockRelations(List<ComStockRelationDto> relationDtoList) {
        return hkStockRelationService.updateStockRelations(relationDtoList);
    }

    @Override
    public Long buildStockId(ComStockRelationDto relationDto) {
        return hkStockRelationService.buildStockId(relationDto);
    }

    @Override
    public Map<String, Date> selectTimeByCodes(List<String> codes) {
        List<StockDefine> stockDefines = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery().in(CollUtil.isNotEmpty(codes), StockDefine::getCode, codes));
        return stockDefines.stream().collect(Collectors.toMap(StockDefine::getCode, StockDefine::getCreateTime, (o, v) -> v));
    }

    @Override
    public Map<String, String> selectReuseCodeMap(List<String> codes) {
        return hkStockRelationService.selectReuseCodeMap(codes);
    }
    /**
     * 根据股票code和业务时间获取该业务时间时的股票id
     *
     * @param codeTimeMap List<Pair<String, String>> left-stockCode  right-timeStr(yyyy-MM-dd)
     *
     * @return Map<Pair < String, String>, Long>，key-Pair<stockCode, timeStr>，value-stockId
     */
    @Override
    public Map<Pair<String, String>, Long> getStockIdByCodeAndTime(List<Pair<String, String>> codeTimeMap) {
        Map<Pair<String, String>, Long> stockIdMap = new HashMap<>();
        List<String> codes = codeTimeMap.stream().map(o -> o.getKey()).collect(Collectors.toList());
        List<HkStockRelation> stockRelations = hkStockRelationService.selectByCodes(codes);
        //原code为临时股票或者代码复用时会有多条数据
        Map<String, List<HkStockRelation>> sourceCodeMap = stockRelations.stream().collect(Collectors.groupingBy(o -> o.getSourceCode()));
        Map<String, HkStockRelation> innerCodeMap = stockRelations.stream().collect(Collectors.toMap(o -> o.getInnerCode(), o -> o, (k1, k2) -> k1));
        Set<String> sourceCodes = sourceCodeMap.keySet();
        Map<String, List<ReuseTempDTO>> tempStockMap = new HashMap<>();
        if (CollUtil.isNotEmpty(sourceCodes)) {
            List<ReuseTempDTO> tempStocks = utsInfoService.findTempStockInfoByCodes(Lists.newArrayList(sourceCodes));
            tempStockMap = tempStocks.stream().collect(Collectors.groupingBy(tempStock -> tempStock.getCode()));
        }
        Date now = new Date();
        for (Pair<String, String> o : codeTimeMap) {
            String code = o.getKey();
            Long busTime = com.vv.finance.common.utils.DateUtils.parseDate(o.getValue()).getTime();
            List<HkStockRelation> sourceCodeStockRelations = sourceCodeMap.get(code);
            HkStockRelation innerCodeStockRelation = innerCodeMap.get(code);
            List<ReuseTempDTO> reuseTempDTOs = tempStockMap.get(code);
            Long stockId = null;
            if (CollUtil.isNotEmpty(sourceCodeStockRelations)) {
                for (HkStockRelation hkStockRelation : sourceCodeStockRelations) {
                    //股票为临时股票
                    if (CollUtil.isNotEmpty(reuseTempDTOs)) {
                        List<ReuseTempDTO> tempDTOS = reuseTempDTOs.stream().filter(reuseTempDTO -> reuseTempDTO.getStartTime() <= busTime && reuseTempDTO.getEndTime() >= busTime).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(tempDTOS)) {
                            ReuseTempDTO reuseTemp = tempDTOS.stream().max(Comparator.comparing(ReuseTempDTO::getEndTime)).get();
                            //临时股票时：历史临时股票则取业务时间在临时股票交易时间段内且关联表的业务时间与临时股票的结束时间一致的股票id；当前临时股票则取业务时间在临时股票交易时间段内且临时股票的结束时间大于等于当前时间
                            if ((StringUtils.isNotBlank(hkStockRelation.getBizTime()) && hkStockRelation.getBizTime().equals(com.vv.finance.common.utils.DateUtils.formatDate(reuseTemp.getEndTime(), "yyyyMMdd"))) ||
                                    (StringUtils.isBlank(hkStockRelation.getBizTime()) && reuseTemp.getStartTime() <= now.getTime() && reuseTemp.getEndTime() >= now.getTime())) {
                                stockId = hkStockRelation.getStockId();
                                continue;
                            }
                        } else {
                            //临时为临时股票不一定当前也是临时股票
                            if (StockRelationStatusEnum.NORMAL.getCode().equals(hkStockRelation.getSecurityStatus())) {
                                stockId = hkStockRelation.getStockId();
                                continue;
                            }
                        }
                    } else {
                        //非临时股票则取当前正常的股票
                        if (StockRelationStatusEnum.NORMAL.getCode().equals(hkStockRelation.getSecurityStatus())) {
                            stockId = hkStockRelation.getStockId();
                            continue;
                        }
                    }
                }
            } else if (ObjectUtil.isNotEmpty(innerCodeStockRelation)) {
                //转板等变更了股票代码的股票
                stockId = innerCodeStockRelation.getStockId();
            }
            stockIdMap.put(o, stockId);
        }
        return stockIdMap;
    }
    /**
     * 获取指定日期发生转板/代码复用的股票信息
     *
     * @param bizTypes 业务类型，2-代码复用，3-转板
     * @param bizTime 业务时间，格式：yyyyMMdd，如临时股票存并行交易结束时间，代码复用或转板存对应变更时间
     * @return
     */
    @Override
    public List<HkStockRelation> getReusOrConversionStock(List<Integer> bizTypes, String bizTime) {
        return hkStockRelationService.selectByBizTypeAndBizTime(bizTypes,bizTime);
    }
    /**
     * 变更盘口数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateOrderStockCode(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更盘口股票code数据 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            Order order = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(order)) {
                order.setCode(targetCode);
                redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(sourceCode));
                redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(targetCode),order);
            }
            log.info("变更盘口股票code数据 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更盘口股票code数据：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }
    }

    /**
     * 变更盘口数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateEconomy(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更盘口股票code数据 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            OrderBrokerDto dto = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(dto)) {
                dto.setCode(targetCode);
                redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(sourceCode));
                redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(targetCode), dto);
            }

            OrderBrokerDto dtoB = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(dtoB)) {
                dtoB.setCode(targetCode);
                redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(sourceCode));
                redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(targetCode), dtoB);
            }


            log.info("变更盘口股票code数据 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更盘口股票code数据：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }
    }

    /**
     * 变更经济席位数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateSimulateEconomy(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更经济席位code数据 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            OrderBrokerDto dto = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(dto)) {
                dto.setCode(targetCode);
                redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(targetCode), dto);
            }

            OrderBrokerDto dtoB = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(dtoB)) {
                dtoB.setCode(targetCode);
                redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(targetCode), dtoB);
            }

            log.info("变更经济席位code数据 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更经济席位code数据：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }
    }

    /**
     * 删除临时股票经济席位数据
     *
     * @param code 时股票
     */
    @Override
    public void delEconomy(String code) {
        try {
            long l = System.currentTimeMillis();
            log.info("删除临时股票经济席位数据 开始：code：{} ",code);
            redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(code));
            redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(code));
            log.info("删除临时股票经济席位数据 结束：code：{} 耗时：{}",code,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("删除临时股票经济席位数据：code：{} 异常",code,e);
        }
    }

    /**
     * 删除临时股票盘口数据
     *
     * @param code 时股票
     */
    @Override
    public void delOrderStockCode(String code) {
        try {
            long l = System.currentTimeMillis();
            log.info("删除临时股票盘口数据 开始：code：{} ",code);
            redisClient.del(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(code));
            log.info("删除临时股票盘口数据 结束：code：{} 耗时：{}",code,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("删除临时股票盘口数据：code：{} 异常",code,e);
        }
    }
    /**
     * 新增模拟股票盘口数据
     *
     * @param simulateCode 模拟股票code
     */
    @Override
    public void saveSimulateOrderInfo(String simulateCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("新增模拟股票盘口数据 开始：simulateCode：{}",simulateCode);
            Long stockId = stockRelationMapper.selectStockIdByInnerCode(simulateCode);
            if (null!=stockId){
                String realCode = simulateCode.replace("-t","" );
                Order order = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(realCode));
                if (ObjectUtils.isNotEmpty(order)) {
                    order.setCode(simulateCode);
                    order.setStockId(stockId);
                    redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(simulateCode),order);
                }
                log.info("新增模拟股票盘口数据 结束：simulateCode：{} 耗时：{}",simulateCode,System.currentTimeMillis()-l);
            }else {
                log.error("新增模拟股票盘口数据 失败：simulateCode：{} 查不到stockId", simulateCode);
            }
        }catch (Exception e){
            log.error("新增模拟股票盘口数据：simulateCode：{} 异常",simulateCode,e);
        }
    }
    /**
     * 校验股票id并返回无效的股票id
     *
     * @param stockIds 股票id集合
     */
    @Override
    public Set<Long> checkStockIdAndGetInvalidStockId(Set<Long> stockIds) {
        QueryComSimpleStockDefineDto query = QueryComSimpleStockDefineDto.builder().stockIds(stockIds).build();
        Map<String, ComSimpleStockDefine> hkComSimpleStockDefineMap = getStockSimpleInfos(query);
        Set<Long> validStockIds = hkComSimpleStockDefineMap.values().stream().map(o -> o.getStockId()).collect(Collectors.toSet());
        return new HashSet<>(CollUtil.subtract(stockIds,validStockIds));
    }
}
