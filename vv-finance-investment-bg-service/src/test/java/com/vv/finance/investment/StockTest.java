package com.vv.finance.investment;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.StockDefinePageReq;
import com.vv.finance.common.entity.quotation.common.ComTradeReqParams;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.f10.F10StockInformationApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.frontend.v2.StockServiceV2;
import com.vv.finance.investment.bg.api.information.InformationApi;
import com.vv.finance.investment.bg.api.information.InformationAppApi;
import com.vv.finance.investment.bg.api.stock.NewStockApi;
import com.vv.finance.investment.bg.api.stock.StockApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.newcode.resp.NewStockListResp;
import com.vv.finance.investment.bg.dto.StockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.information.app.FreeStockNewsVoApp;
import com.vv.finance.investment.bg.entity.req.CompanyEventReq;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;
import com.vv.finance.investment.bg.entity.uts.Xnhk0901;
import com.vv.finance.investment.bg.entity.uts.Xnhks0601;
import com.vv.finance.investment.bg.job.stock.NewStockTask;
import com.vv.finance.investment.bg.job.stock.TradeCapitalTask;
import com.vv.finance.investment.bg.job.uts.TradingCalendarJob;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.TradeCalendar;
import com.vv.finance.investment.bg.stock.info.dto.StockShortSale;
import com.vv.finance.investment.bg.stock.info.dto.SuspensionDto;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockRelatedDetailsService;
import com.vv.finance.investment.bg.stock.trade.mapper.TradeCalendarMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName StockTest
 * @Deacription 股票相关接口test
 * @Author lh.sz
 * @Date 2021年10月09日 16:50
 **/
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class StockTest {

    @Resource
    StockServiceV2 stockServiceV2;
    @Resource
    RedisClient redisClient;
    @Resource
    StockDefineMapper stockDefineMapper;
    @Resource
    StockApi stockApi;
    @Resource
    F10StockInformationApi f10StockInformationApi;
    @Resource
    StockService stockService;
    @Resource
    IStockMarketService stockMarketService;
    @Resource
    IStockRelatedDetailsService stockRelatedDetailsService;
    @Resource
    InformationApi informationApi;


    @Resource
    private NewStockApi newStockApi;
    @Resource
    private NewStockTask newStockTask;

    @Resource
    IStockKlineService iStockKlineService;

    @Resource
    TradeCalendarMapper tradeCalendarMapper;

    //@DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
    //StockService stockService;

    @Resource
    TradingCalendarJob tradingCalendarJob;

    @Resource
    StockCache stockCache;

    @Resource
    StockTradeStatisticsApi stockTradeStatisticsApi;

    @Resource
    StockInfoApi stockInfoApi;

    @Resource
    TradeCapitalTask tradeCapitalTask;

    @Resource
    InformationAppApi informationAppApi;

    @Test
    public void tradingCalandarJobtest(){
        tradingCalendarJob.syncTradingCalendar("");
    }

    @Test
    public void updateTradingStatustest(){
        tradingCalendarJob.updateTradingStatus("");
    }

    @Test
    public void modifyCalendarForClose(){
        tradingCalendarJob.modifyCalendarForClose("");
    }

    @Test
    public void tradeCalandarTest(){
        List<TradeCalendar> rdate = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().gt("RDATE", 20230901));
        System.out.println(rdate);
    }
   


    @Test
    public void f10StockInformationApiTest() {
        f10StockInformationApi.changeBeforeAfterDividend("00241.hk");
    }

    @Test
    public void companyEventTest() {
        CompanyEventReq req = CompanyEventReq.builder().code("00700.hk").type("month").build();
        f10StockInformationApi.hkCompanyEvent(req);
    }


    @Test
    void queryAllKline() {
        KlineReq klineReq = new KlineReq();
        klineReq.setCode("00737.hk");
        klineReq.setType("year");
        klineReq.setEndTime(1710991278091l);
        klineReq.setCurrent(1);
        klineReq.setAdjhkt("not");
        klineReq.setPageSize(500);
//        iStockKlineService.queryAllKline(klineReq);
    }


    @Test
    void checkDel() {
        redisClient.del(RedisKeyConstants.RECEIVER_SECURITY_STATUS_BEAN.concat("11111"));
    }


    @Test
    void checkSup() {
        Set<String> set = redisClient.getKeys(RedisKeyConstants.RECEIVER_SECURITY_STATUS_BEAN.concat("*"));
        List<String> dtoList = new ArrayList<>();
        set.forEach(s -> {
            SuspensionDto suspensionDto = redisClient.get(s);
            dtoList.add(suspensionDto.getCode());
        });
        List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().eq("suspension", 2));
        List<String> code = stockDefines.stream().map(StockDefine::getCode).filter(s -> !dtoList.contains(s)).collect(Collectors.toList());
        log.info(JSON.toJSONString(code));
    }

    @Test
    void checkSe() {
        stockApi.getStockSecurityStatus("00700.hk");
    }

    @Test
    void test1() {
        stockService.getSnapshotList(new String[]{"11117.hk"});
    }

    @Test
    void testGetWarrant() {
        String json = redisClient.hget(RedisKeyConstants.RECEIVER_WARRANT_MAP_STOCK_SNAPSHOT, "19298.hk");
        log.info(json);
    }


    @Test
    void testCheckCache() {
        stockMarketService.getXnhks0501(Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())));
    }

    @Test
    void getNullCode() {
        List<StockRelatedDetails> codes = stockRelatedDetailsService.list(new QueryWrapper<StockRelatedDetails>().eq("type","0"));
        List<StockSnapshot> snapshotList = codes.stream().map(s->{
            StockSnapshot snapshot = JSON.parseObject(s.getSnapshotDetails(),StockSnapshot.class);
            return snapshot;
        }).collect(Collectors.toList());
        List<String> nullCodes = snapshotList.stream().filter(s->s.getPreClose().compareTo(BigDecimal.ZERO) ==0).map(StockSnapshot::getCode).collect(Collectors.toList());
        log.info(JSON.toJSONString(nullCodes));
    }


    @Test
    void checkXnhk0901() {
        List<String> codes = new ArrayList<>();
        codes.add("00700.hk");
        codes.add("00001.hk");
        List<Xnhk0901> list = stockMarketService.getXnhk0901List(codes).getData();
        log.info(JSON.toJSONString(list));
    }
    @Test
    void testGetHot(){
        // List<StockQueryDTO> stockQueryDTOS = stockService.queryStock("00526.hk", false, false);
        List<StockQueryDTO> stockQueryDTOS = stockService.queryStock("08596.hk", false, false);
        System.out.println(stockQueryDTOS);

    }

    @Test
    void queryNotice() {
        List<String> codes = new ArrayList<>();
        CalendarNewsPageReq calendarNewsPageReq = new CalendarNewsPageReq();
        calendarNewsPageReq.setPageSize(50l);
        ResultT<PageWithTime<InformationGroupVo<FinancialReportVo>>> resultT = informationApi.queryNotice(codes, calendarNewsPageReq);
        log.info(JSON.toJSONString(resultT));
    }

    @Test
    void pageTradeListPc() {
        ComTradeReqParams params = new ComTradeReqParams();
        params.setCode("09888.hk");
        params.setCurrent(1L);
        params.setPageSize(315L);
        params.setPageTurnType(-1);
        params.setTime("1696918058157923800");
//        stockServiceV2.pageTradeListPc(params);
    }


    @Test
    void saveAllNewStocksToRedisTest(){
        newStockTask.saveAllNewStocksToRedis("");
    }

    @Test
    void getNewStockCodeListTest() {
        ResultT<List<StockCodeNameBaseDTO>> resultT = newStockApi.getNewStockCodeList(null, null);
        log.info("{}", resultT);
//        resultT = newStockApi.getNewStockCodeList("desc", "peTtm");
        log.info("{}", JSONUtil.toJsonStr(resultT));
    }

    @Test
    void getNewStockInfoListTest() {
        String[] codeList = {"09860.hk","02481.hk"};
        ResultT<List<NewStockListResp>> resultT = newStockApi.getNewStockInfoList(codeList);
        log.info("{}", JSONUtil.toJsonStr(resultT));
    }

    @Test
    void getStockShortSale() {
//        ResultT<List<StockShortSale>> stockShortSale = stockApi.getStockShortSale("00700.hk");
        System.err.println(1);
    }

    @Test
    void getStockInfo() {
        // ResultT<List<StockBaseInfoDTO>> stockInfo = stockApi.getStockInfo(null, "down", "chgPct", null);
        ResultT<List<StockBaseInfoDTO>> stockInfo2 = stockApi.getStockInfo(null, "down", "fiveMinutesChgPct", null);
        System.err.println(1);
    }

    // @Test
    // void updateStockAndName() {
    //     stockCache.updateStockAndName("00700.hk", "腾讯股份有限公司");
    //     System.err.println(1);
    // }

//    @Test
//    void listFinancialEvents() {
//        //startTime=1704681199566&endTime=1704681199566&currentPage=1&pageSize=200
//        CalendarNewsPageReq pageReq = new CalendarNewsPageReq();
//        pageReq.setStartTime(1704681199566l);
//        pageReq.setEndTime(1704681199566l);
//        pageReq.setCurrentPage(1l);
//        pageReq.setPageSize(200l);
//        informationApi.listFinancialEvents(pageReq);
//        System.err.println(1);
//    }

    @Test
    void updateSuspension() {
        SuspensionDto suspensionDto = new SuspensionDto();
        suspensionDto.setCode("00700.hk");
        suspensionDto.setSuspension("1");
        stockInfoApi.updateSuspension(suspensionDto);
        System.err.println(1);
    }

    @Test
    void queryPreviousTradeNum() {
        Set<String> stockCodes = new ConcurrentHashSet<>();
        stockCodes.add("TSLA.us");
        stockCodes.add("AAPL.us");
        stockCodes.add("01330.hk");
        stockCodes.add("00700.hk");
        stockTradeStatisticsApi.queryPreviousTradeNum(stockCodes);
    }

    @Test
    void getBlockSnapshot() {
        stockInfoApi.getBlockSnapshot();
    }

    @Test
    public void saveDdeBySizeCriterion(){

//        tradeCapitalTask.saveDdeBySizeCriterion(null);

    }

    @Test
    public void delDdeBySizeCriterion(){

        tradeCapitalTask.delDdeBySizeCriterion(null);

    }

    @Test
    void gatStockByType() {
        stockService.gatStockByType(14, null, null);
    }

    @Test
    void getStockList() {
        stockApi.getStockList(null, null, null,"GEM");
    }


    @Test
    void pageStockDefine() {
        StockDefinePageReq stockDefinePageReq = new StockDefinePageReq();
        stockDefinePageReq.setStockCode("00700.hk");
        stockDefinePageReq.setPageSize(10l);
        stockDefinePageReq.setCurrentPage(1l);
        System.out.println(stockInfoApi.pageStockDefine(stockDefinePageReq));

        StockDefinePageReq stockDefinePageReq2 = new StockDefinePageReq();
        stockDefinePageReq2.setStockCode("00700");
        stockDefinePageReq2.setPageSize(10l);
        stockDefinePageReq2.setCurrentPage(1l);
        System.out.println(stockInfoApi.pageStockDefine(stockDefinePageReq2));
    }

    @Test
    public void getS0601ListByCodes(){
        Set<String> codes = new HashSet<>();
        codes.add("00700.hk");
        codes.add("00916.hk");

        ResultT<List<Xnhks0601>> rdate = stockMarketService.getXnhks0601ListByCodes(codes);
        System.out.println(rdate);
    }

    @Test
    public void listNewsBySimpleStockVoV2(){
        CommonNewsPage commonNewsPage  = new CommonNewsPage();
        commonNewsPage.setQueryCode(5);
        commonNewsPage.setStockId(1000000603l);
        commonNewsPage.setStockCode("00700.hk");
        ResultT<PageWithTime<FreeStockNewsVoApp>> resultT = informationAppApi.listNewsBySimpleStockVoV2(commonNewsPage);
        System.out.println(resultT);
    }

    @Test
    public void saveBatchV2(){
        List<TradeStatistics> tradeStatisticsList = new ArrayList<>();
        TradeStatistics tradeStatistics = new TradeStatistics();
        tradeStatistics.init(System.currentTimeMillis(), "00700.hk");
        tradeStatisticsList.add(tradeStatistics);
        stockTradeStatisticsApi.saveBatchV2(tradeStatisticsList);
    }
}
