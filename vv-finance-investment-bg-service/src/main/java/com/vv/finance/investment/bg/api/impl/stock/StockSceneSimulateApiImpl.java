package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockRelationDto;
import com.vv.finance.common.entity.quotation.common.ComSceneReq;
import com.vv.finance.common.enums.StockRelationBizEnum;
import com.vv.finance.common.utils.ConcatCodeUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.JsonUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.f10.F10TableTemplateApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.information.InformationAppApi;
import com.vv.finance.investment.bg.api.lineshape.LineshapeService;
import com.vv.finance.investment.bg.api.southward.SouthwardCapitalApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.stock.StockMoveApi;
import com.vv.finance.investment.bg.api.stock.StockSceneSimulateApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.entity.uts.Xnhk0307;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.HkStockScene;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.TradeCalendar;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockSceneSimulateMapper;
import com.vv.finance.investment.bg.stock.info.service.HkStockRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hqj
 * @date 2020/10/28 11:02
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@RequiredArgsConstructor
@Slf4j
public class StockSceneSimulateApiImpl implements StockSceneSimulateApi {

    @Resource
    StockSceneSimulateMapper stockSceneSimulateMapper;
    @Resource
    private HkStockRelationMapper hkStockRelationMapper;
    @Resource
    private StockDefineMapper stockDefineMapper;
    @Resource
    private StockMoveApi stockMoveApi;
    @Resource
    private StockTradeStatisticsApi stockTradeStatisticsApi;
    @Resource
    private StockService stockService;
    @Resource
    private LineshapeService lineshapeService;
    @Resource
    private InformationAppApi informationAppApi;
    @Resource
    private UtsInfoService utsInfoService;
    @Resource
    private StockInfoApi stockInfoApi;
    @Resource
    private HkStockRelationService hkStockRelationService;
    @Resource
    private BrokerAnalysisApi brokerAnalysisApi;
    @Resource
    private F10TableTemplateApi f10TableTemplateApi;
    @Resource
    private TrendsService trendsService;
    @Resource
    private RedisClient redisClient;
    @Resource
    private StockCache stockCache;
    @Resource
    private SouthwardCapitalApi southwardCapitalApi;
    /**
     * 并行交易结束次日 通过日期获取当天结束的并行交易的股票代码
     * 注意：这里我查询的就是传入进来的日期
     *
     * @param date
     */
    @Override
    public ResultT<List<ReuseTempDTO>> findEndTradeTempSimulateStock(Date date) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_END_DATE, DateUtils.formatDateToLong(date,null))
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.TEMP.getCode()));
        List<ReuseTempDTO> list = buildReuseTemp(hkStockScenes);
        //模拟临时股票stockId赋值
        buildStockId(list);
        log.info("获取指定日期发生的结束的并行交易的股票代码记录，入参日期：{}，变更记录：{}", DateUtils.formatDateToLong(date,null), JsonUtils.beanToJson(list));
        return ResultT.success(list);
    }
    //模拟临时股票stockId赋值
    private void buildStockId(List<ReuseTempDTO> reuseTemps) {
        if (CollUtil.isNotEmpty(reuseTemps)) {
            List<String> codes = reuseTemps.stream().map(reuseTemp -> reuseTemp.getCode()).collect(Collectors.toList());
            List<HkStockRelation> stockRelations = hkStockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, codes));
            if (CollUtil.isNotEmpty(stockRelations)) {
                //原code为临时股票或者代码复用时会有多条数据
                Map<String, List<HkStockRelation>> sourceCodeMap = stockRelations.stream().collect(Collectors.groupingBy(o -> o.getSourceCode()));
                Date now = new Date();
                reuseTemps.forEach(reuseTemp->{
                    List<HkStockRelation> tempStockRelations = sourceCodeMap.get(reuseTemp.getCode());
                    if (CollUtil.isNotEmpty(tempStockRelations)) {
                        List<HkStockRelation> relations = tempStockRelations.stream().filter(tempRelation -> {
                            if ((StringUtils.isNotBlank(tempRelation.getBizTime()) && tempRelation.getBizTime().equals(DateUtils.formatDate(reuseTemp.getEndTime(), "yyyyMMdd"))) ||
                                    (StringUtils.isBlank(tempRelation.getBizTime()) &&  reuseTemp.getStartTime() <= now.getTime() && reuseTemp.getEndTime() >= now.getTime()) ||
                                    (StringUtils.isNotBlank(tempRelation.getBizTime()) && tempRelation.getBizTime().equals(DateUtils.formatDate(reuseTemp.getStartTime(), "yyyyMMdd"))) ){
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(relations)) {
                            HkStockRelation stockRelation = relations.get(0);
                            reuseTemp.setStockId(stockRelation.getStockId());
                        }
                    }
                });
            }
        }
    }

    /**
     * 并行交易开始当天 通过日期获取当天开始的并行交易的股票代码
     *
     * @param date
     */
    @Override
    public List<ReuseTempDTO> findTradingTempSimulateStockByTime(Date date) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_START_DATE, DateUtils.formatDateToLong(date,null))
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.TEMP.getCode()));
        List<ReuseTempDTO> list = buildReuseTemp(hkStockScenes);
        //模拟临时股票stockId赋值
        buildStockId(list);
        log.info("获取指定日期发生的开始的并行交易的股票代码记录，入参日期：{}，变更记录：{}", DateUtils.formatDateToLong(date,null), JsonUtils.beanToJson(list));
        return list;
    }

    private List<ReuseTempDTO> buildReuseTemp(List<HkStockScene> stockScenes) {
        List<ReuseTempDTO> collect = stockScenes.stream().map(item -> {
            ReuseTempDTO reuseTemp = new ReuseTempDTO();
            reuseTemp.setRelationCode(item.getCode());
            reuseTemp.setCode(item.getSceneCode());
            reuseTemp.setStartTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(item.getSceneStartDate().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
            reuseTemp.setEndTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(item.getSceneEndDate().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
            reuseTemp.setStockName(item.getSceneCode());
            reuseTemp.setRelationStockName(item.getCode());
            return reuseTemp;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 港股代码复用场景，获取指定日期发生的代码复用code
     *
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    @Override
    public ResultT<List<String>> getSimulateStockRepeat(Date date) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_START_DATE, DateUtils.formatDateToLong(date,null))
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.REUSE.getCode()));
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(hkStockScenes)) {
            list = hkStockScenes.stream().map(HkStockScene::getSceneCode).collect(Collectors.toList());
        }
        log.info("获取指定日期发生的模拟代码复用记录，入参日期：{}，变更记录：{}", DateUtils.formatDateToLong(date,null), JsonUtils.beanToJson(list));
        return ResultT.success(list);
    }

    /**
     * 港股转板代码变更场景，获取指定日期发生的代码变更记录
     *
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    @Override
    public ResultT<Map<String, String>> getSimulateStockConversionMarket(String date) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_START_DATE, date)
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.TRANSFER.getCode()));
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(hkStockScenes)) {
            map = hkStockScenes.stream().collect(Collectors.toMap(HkStockScene::getCode, HkStockScene::getSceneCode));
        }
        log.info("获取指定日期发生的模拟代码变更记录，入参日期：{}，变更记录：{}", date, JsonUtils.beanToJson(map));
        return ResultT.success(map);
    }

    /**
     * 特殊场景造数据
     * 1、新股
     * @param
     * @param
     * @return
     */
    @Override
    public ResultT saveDataSceneByCode(ComSceneReq comSceneReq) {
        if(!comSceneReq.getCode().contains("-t")){
            return ResultT.fail("股票格式不正确，未包含-test");
        }

        if(StringUtils.isNotEmpty(comSceneReq.getSceneCode())){
            if(!comSceneReq.getSceneCode().contains("-t")){
                return ResultT.fail("场景股票格式不正确，未包含-test");
            }
        }

        HkStockScene stockScene = new HkStockScene();
        if(comSceneReq.getSceneType() == StockRelationBizEnum.NORMAL.getCode()){
            Integer count = stockSceneSimulateMapper.selectCount(new QueryWrapper<HkStockScene>().eq("code", comSceneReq.getCode()));
            if(count > 0){
                return ResultT.fail("股票已存在");
            }
            stockScene.setCode(comSceneReq.getCode());
            stockScene.setSceneCode(comSceneReq.getCode());
            stockScene.setSceneType(comSceneReq.getSceneType());
            stockScene.setSceneStartDate(Long.valueOf(LocalDate.now().toString().replace("-","")));
            ComStockRelationDto relation = ComStockRelationDto.builder()
                    .sourceCode(comSceneReq.getCode())
                    .innerCode(comSceneReq.getCode())
                    .bizType(StockRelationBizEnum.SIMULATE.getCode())
                    .remark("模拟新股").build();
            stockSceneSimulateMapper.insert(stockScene);
            hkStockRelationService.buildStockId(relation);
            String code = stockScene.getSceneCode().replace("-t","");
            // 造行情数据
            this.copyQuotation(code, stockScene.getSceneCode());
        }else if(comSceneReq.getSceneType() == StockRelationBizEnum.TEMP.getCode()){
            stockScene.setCode(comSceneReq.getCode());
            stockScene.setSceneCode(comSceneReq.getSceneCode());
            stockScene.setSceneType(comSceneReq.getSceneType());
            stockScene.setClosedTradingTime(comSceneReq.getClosedTradingTime());
            stockScene.setSuspendTradingTime(comSceneReq.getSuspendTradingTime());
            Long sceneStartDate = null;
            Long sceneEndDate = null;
            if (stockScene.getSuspendTradingTime().contains("/") && stockScene.getSuspendTradingTime().contains("-")) {
                String[] startTimes = stockScene.getSuspendTradingTime().split("-");
                sceneStartDate = Long.valueOf(String.valueOf(startTimes[0]).replace("/", ""));
            }

            if (stockScene.getClosedTradingTime().contains("/") && stockScene.getClosedTradingTime().contains("-")) {
                String[] endTimes = stockScene.getClosedTradingTime().split("-");
                sceneEndDate = Long.valueOf(String.valueOf(endTimes[1]).replace("/", ""));
            }

            stockScene.setSceneStartDate(sceneStartDate);
            stockScene.setSceneEndDate(sceneEndDate);

            ComStockRelationDto relation = ComStockRelationDto.builder()
                    .sourceCode(comSceneReq.getSceneCode())
                    .innerCode(comSceneReq.getSceneCode())
                    .bizType(StockRelationBizEnum.SIMULATE.getCode())
                    .remark(StockRelationBizEnum.TEMP.getDesc()).build();
            stockSceneSimulateMapper.insert(stockScene);
            hkStockRelationService.buildStockId(relation);
        }else if(comSceneReq.getSceneType() == StockRelationBizEnum.REUSE.getCode()){
            stockScene.setCode(comSceneReq.getCode());
            stockScene.setSceneCode(comSceneReq.getCode());
            stockScene.setSceneType(comSceneReq.getSceneType());
            stockScene.setSceneStartDate(comSceneReq.getSceneStartDate());
            stockSceneSimulateMapper.insert(stockScene);
        }else if(comSceneReq.getSceneType() == StockRelationBizEnum.TRANSFER.getCode()){
            stockScene.setCode(comSceneReq.getCode());
            stockScene.setSceneCode(comSceneReq.getSceneCode());
            stockScene.setSceneType(comSceneReq.getSceneType());
            stockScene.setSceneStartDate(comSceneReq.getSceneStartDate());
            stockSceneSimulateMapper.insert(stockScene);
        }else if(comSceneReq.getSceneType() == StockRelationBizEnum.SHAREHOLD.getCode()){
            Integer count = stockSceneSimulateMapper.selectCount(new QueryWrapper<HkStockScene>().eq("code", comSceneReq.getCode()));
            if(count > 0){
                return ResultT.fail("股票已存在");
            }
            stockScene.setCode(comSceneReq.getCode());
            stockScene.setSceneCode(comSceneReq.getCode());
            stockScene.setSceneType(comSceneReq.getSceneType());
            stockScene.setSceneStartDate(comSceneReq.getSceneStartDate());
            stockScene.setSceneEndDate(Long.valueOf(LocalDate.now().toString().replace("-","")));

            ComStockRelationDto relation = ComStockRelationDto.builder()
                    .sourceCode(comSceneReq.getCode())
                    .innerCode(comSceneReq.getCode())
                    .bizType(StockRelationBizEnum.SIMULATE.getCode())
                    .remark("模拟股权股票").build();
            stockSceneSimulateMapper.insert(stockScene);
            hkStockRelationService.buildStockId(relation);
            String code = stockScene.getSceneCode().replace("-t","");
            // 造行情数据
            this.copyQuotation(code, stockScene.getSceneCode());
        }
        return ResultT.success();
    }


    private void copyQuotation(String sourceCode, String targetCode){
        // 拷贝快照数据
        stockService.copySnapshotStockCode(sourceCode, targetCode);
        // 拷贝资金数据
        stockTradeStatisticsApi.copyTradeStatisticStockCode(sourceCode, targetCode, true);
        //拷贝异动数据
        stockMoveApi.copyStockMoveStockCode(sourceCode, targetCode);
        //处理形态数据
        lineshapeService.saveSimulateLineshapeInfo(targetCode);
        //处理公告数据
        utsInfoService.saveSimulateNoticeInfo(targetCode);
        //处理盘口数据
        stockInfoApi.saveSimulateOrderInfo(targetCode);
        //处理资讯数据
        informationAppApi.saveSimulateInformation(targetCode);
        // 公司动向
        trendsService.createCompanyTrendByStockCode(targetCode);
        // 经纪商数据
        brokerAnalysisApi.createBrokerDataByCode(targetCode);
        // F10财务
        f10TableTemplateApi.createF10DataByCode(targetCode);
        //处理经济席位数据
        stockInfoApi.updateSimulateEconomy(sourceCode,targetCode);
        // 模拟南向资金净流入
        southwardCapitalApi.createSouthwardDataByCode(targetCode);
        // 刷新rank缓存
//        industryKlineApi.updateStockRankCache(RegionTypeEnum.HK.getCode());
        // 处理uts数据
        utsInfoService.handleUtsDataByCodeAndType(targetCode, null, String.valueOf(SqlCommandType.INSERT));
    }

    @Override
    public void deleteDataSceneByCode(boolean isMock, String code) {
        if (isMock) {
            Assert.isTrue(StrUtil.isNotBlank(code) && StrUtil.contains(code, "-t"), "输入的code: {} 不能为空且须包含-t！");
            // 删除码表、关联表、场景表
            stockDefineMapper.delete(Wrappers.<StockDefine>lambdaQuery().eq(StockDefine::getCode, code));
            hkStockRelationMapper.delete(Wrappers.<HkStockRelation>lambdaQuery().eq(HkStockRelation::getInnerCode, code));
            stockSceneSimulateMapper.delete(Wrappers.<HkStockScene>lambdaQuery().eq(HkStockScene::getCode, code));
        }
        // 删除形态数据
        lineshapeService.delLineshapeByStockCode(code);
        // 处理公告数据
        utsInfoService.delNoticeByStockCode(code);
        // 删除盘口数据
        stockInfoApi.delOrderStockCode(code);
        // 删除盘口数据
        stockInfoApi.delEconomy(code);
        // 删除资讯数据
        informationAppApi.delByStockCode(code);
        // 删除公司动向
        trendsService.delCompanyTrendByStockCode(code);
        // 删除异动数据
        stockMoveApi.delStockMoveByStockCode(code);
        // 删除资金数据股票
        stockTradeStatisticsApi.delTradeStatisticByStockCode(code);
        // 删除快照数据
        stockService.delSnapshotByStockCode(code);
        // 删除F10数据
        f10TableTemplateApi.deleteF10DataByCode(code);
        // 删除经纪商数据
        brokerAnalysisApi.deleteBrokerDataByCode(code);
        // 刷新缓存
        // stockCache.updateStockSimpleInfo();
        stockInfoApi.sendUpdateMessage();
        // 刷新rank缓存
//        industryKlineApi.updateStockRankCache(RegionTypeEnum.HK.getCode());
        // 删除k线
//        hkStockMinApi.deleteTempKline(Arrays.asList(code));
//        hkStockCompositeApi.deleteTempKline(Arrays.asList(code));
        // 模拟南向资金净流入
        southwardCapitalApi.deleteSouthwardDataByCode(code);
        // 处理uts数据
        // 处理uts数据
        if (isMock) {
            utsInfoService.handleUtsDataByCodeAndType(code, null, String.valueOf(SqlCommandType.DELETE));
        }
    }

    @Override
    public void copyQuotationByCode(String code) {
        //处理盘口数据
        stockInfoApi.saveSimulateOrderInfo(code);

        String realCode = code.replace("-t","" );
        Map<String, String> mappings = new HashMap<>();
        mappings.put(realCode, code);
        log.info("模拟变更历史日周月季年k数据开始");
//        hkStockCompositeApi.dealSimulateKline(mappings);
        log.info("模拟变更历史日周月季年k数据结束");
        log.info("模拟变更历史分k数据开始");
//        hkStockMinApi.dealSimulateKline(mappings);
        log.info("模拟变更历史分k数据结束");
        // 拷贝快照数据
        stockService.copySnapshotStockCode(realCode, code);
        // 刷新rank缓存
//        industryKlineApi.updateStockRankCache(RegionTypeEnum.HK.getCode());
    }

    @Override
    public ResultT updateSceneDate(List<String> codes) {
        if(CollectionUtils.isEmpty(codes)){
            return ResultT.success();
        }
        Long date = Long.valueOf(LocalDate.now().toString().replace("-", ""));
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>().in("code", codes).eq("scene_start_date", date));
        for (HkStockScene hkStockScene : hkStockScenes) {
            hkStockScene.setSceneStartDate(19700101l);
            hkStockScene.setRemark("已经触发过特殊场景了");
            stockSceneSimulateMapper.update(hkStockScene, new UpdateWrapper<HkStockScene>().in("code", codes));
        }
        return ResultT.success();
    }

    /**
     * 获取指定日期开始交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getStartTradingStockRights(Date time) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_START_DATE, DateUtils.formatDateToLong(time,null))
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.SHAREHOLD.getCode()));
        List<StockRightsDTO> stockRightsDTOS = buildStockRightsStockId(hkStockScenes);
        return stockRightsDTOS;
    }
    /**
     * 获取指定日期结束交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getEndTradingStockRights(Date time) {
        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_END_DATE, DateUtils.formatDateToLong(time,null))
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.SHAREHOLD.getCode()));

        List<StockRightsDTO> stockRightsDTOS = buildStockRightsStockId(hkStockScenes);
        return stockRightsDTOS;
    }

    //股权股票stockId赋值
    private List<StockRightsDTO> buildStockRightsStockId(List<HkStockScene> stockScenes) {
        List<StockRightsDTO> stockRightsDTOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(stockScenes)) {
            List<String> codes = stockScenes.stream().map(stockRightsDTO -> stockRightsDTO.getCode()).collect(Collectors.toList());
            List<HkStockRelation> stockRelations = hkStockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, codes));
            if (CollUtil.isNotEmpty(stockRelations)) {
                //原code为股权股票或者代码复用时会有多条数据
                Map<String, List<HkStockRelation>> sourceCodeMap = stockRelations.stream().collect(Collectors.groupingBy(o -> o.getSourceCode()));
                long currentDate = DateUtils.formatDateToLong(new Date(),null);
                stockScenes.forEach(stockRights->{
                    List<HkStockRelation> stockRightsRelations = sourceCodeMap.get(stockRights.getCode());
                    if (CollUtil.isNotEmpty(stockRightsRelations)) {
                        List<HkStockRelation> relations = stockRightsRelations.stream().filter(stockRightsRelation -> {
                            if ((StringUtils.isNotBlank(stockRightsRelation.getBizTime()) && stockRightsRelation.getBizTime().equals(stockRights.getSceneEndDate().toString())) ||
                                    (StringUtils.isBlank(stockRightsRelation.getBizTime()) &&  stockRights.getSceneStartDate() <= currentDate && stockRights.getSceneEndDate() >= currentDate) ||
                                    (StringUtils.isNotBlank(stockRightsRelation.getBizTime()) && stockRightsRelation.getBizTime().equals(stockRights.getSceneStartDate().toString())) ){
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        StockRightsDTO stockRightsDTO = new StockRightsDTO();
                        stockRightsDTO.setCode(stockRights.getSceneCode());
                        stockRightsDTO.setStartListingDate(LocalDateTimeUtil.getTimestamp(LocalDate.parse(stockRights.getSceneStartDate().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
                        stockRightsDTO.setEndListingDate(LocalDateTimeUtil.getTimestamp(LocalDate.parse(stockRights.getSceneEndDate().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
                        stockRightsDTO.setName(stockRights.getSceneCode());
                        if (CollUtil.isNotEmpty(relations)) {
                            HkStockRelation stockRelation = relations.get(0);
                            stockRightsDTO.setStockId(stockRelation.getStockId());
                        }
                        stockRightsDTOS.add(stockRightsDTO);
                    }
                });
            }
        }
        return stockRightsDTOS;
    }
}
