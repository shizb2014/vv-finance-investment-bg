package com.vv.finance.investment;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.domain.PageWithCount;
import com.vv.finance.common.entity.quotation.f10.ComNewShareVo;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.f10.F10StockInformationApi;
import com.vv.finance.investment.bg.api.impl.f10.F10TableTemplateApiImpl;
import com.vv.finance.investment.bg.api.impl.stock.StockMoveApiImpl;
import com.vv.finance.investment.bg.api.information.InformationApi;
import com.vv.finance.investment.bg.api.stock.StockMoveApi;
import com.vv.finance.investment.bg.api.stock.StockSceneSimulateApi;
import com.vv.finance.investment.bg.api.uts.IMainBusinessService;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.f10.DirectorManager;
import com.vv.finance.investment.bg.dto.f10.F10PageBaseReq;
import com.vv.finance.investment.bg.dto.f10.SecuritiesInformation;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.dto.uts.resp.StockUtsBasicFactsResp;
import com.vv.finance.investment.bg.dto.uts.resp.StockUtsNoticeListResp;
import com.vv.finance.investment.bg.dto.uts.resp.ValuationGrowth;
import com.vv.finance.investment.bg.dto.uts.resp.*;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10TableTemplate;
import com.vv.finance.investment.bg.entity.f10.RatingsTableEntity;
import com.vv.finance.investment.bg.entity.f10.f10Profit.F10ProfitEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.F10CommonFinTable;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.MainBusinessData;
import com.vv.finance.investment.bg.entity.f10.trends.GeneralMeeting;
import com.vv.finance.investment.bg.entity.f10.trends.TransactionAlert;
import com.vv.finance.investment.bg.entity.move.StockMove;
import com.vv.finance.investment.bg.entity.uts.HkexTd;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk0205;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.job.uts.DataMonitorJob;
import com.vv.finance.investment.bg.job.uts.TrendsJob;
import com.vv.finance.investment.bg.mapper.uts.StopAndResumeMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0205Mapper;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10ChartServiceImpl;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10SourceServiceImpl;
import com.vv.finance.investment.gateway.api.broker.BrokerInfoServiceApi;
import com.vv.finance.investment.gateway.dto.req.BrokerStatisticsParams;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerDetailsRes;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerStatisticsRes;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.Collator;
import java.time.LocalDate;
import java.util.*;

/**
 * @author hamilton
 * @date 2020/10/30 14:27
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class utsTest {

    @Autowired
    private RedisClient redisClient;

    @Resource
    private UtsInfoService utsInfoService;

    @Resource
    private F10TableTemplateApiImpl f10TableTemplateApi;

    @Resource
    F10SourceServiceImpl f10SourceService;

    @Resource
    F10KeyFiguresDao f10KeyFiguresDao;

    @Resource
    F10CashFlowDao f10CashFlowDao;

    @Resource
    F10ChartServiceImpl f10ChartService;
    @Resource
    F10StockInformationApi f10StockInformationApi;
    @Resource
    TrendsService trendsService;
    @Resource
    StopAndResumeMapper stopAndResumeMapper;
    @Resource
    private Xnhk0205Mapper xnhk0205Mapper;
    @Resource
    StockMoveApiImpl stockMoveApi;
    @Resource
    IStockMarketService stockMarketService;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    BrokerInfoServiceApi brokerInfoServiceApi;
    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;
    @Resource
    IStockMarketService iStockMarketService;
    @Resource
    IMainBusinessService mainBusinessService;

    @Resource
    StockSceneSimulateApi stockSceneSimulateApi;

    @Resource
    DataMonitorJob dataMonitorJob;

    @Resource
    InformationApi informationApi;

    @Test
    public void getBusinessDataTest(){
        MainBusinessData generalMeeting2 = mainBusinessService.getBusinessData("00999.hk");
        System.out.println(generalMeeting2);
    }

    @Test
    public void getGeneralMeeting2Test(){
        SimplePageReq req = new SimplePageReq();
        req.setCurrentPage(1l);
        req.setPageSize(10l);
        PageDomain<GeneralMeeting> generalMeeting2 = trendsService.getGeneralMeeting2("03968.hk",req);
        System.out.println(generalMeeting2);
    }

    @Test
    public void getTransactionAlert2Test(){
        SimplePageReq req = new SimplePageReq();
        req.setCurrentPage(1l);
        req.setPageSize(10l);
        PageDomain<TransactionAlert> transactionAlert2 = trendsService.getTransactionAlert2("00020.hk",req);
        System.out.println(transactionAlert2);
    }

    @Test
    public void getBasicFactsTest(){
        StockUtsBriefingResp basicFacts = utsInfoService.getBriefing("08566.hk");
        System.out.println(basicFacts);
    }

    @Resource
    TrendsJob trendsJob;



    @Test
    public void calTest(){
        HkexTd hkexTd = hkTradingCalendarApi.queryBeginTradingCalendars(10000);
        System.out.println(hkexTd);
    }


    @Test
    public void valuationTest(){
        SimplePageReq simplePageReq=new F10PageBaseReq();
        simplePageReq.setPageSize(10L);
        simplePageReq.setCurrentPage(1L);
        SimplePageResp<ValuationGrowth> valuationGrowthSimplePageResp = stockMarketService.valuationGrowth(simplePageReq);

        System.out.printf(valuationGrowthSimplePageResp.toString());

    }

    @Test
    public void groupQuery() {
        List<Xnhk0205> xnhk0205s = xnhk0205Mapper.selectList(new QueryWrapper<Xnhk0205>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        System.out.println(xnhk0205s);
    }

    @Test
    public void testPageListByTypeListPc() {
        String entityListKey =  RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST;
//        StockMoveTheme stockMoveTheme = new StockMoveTheme();
//        stockMoveTheme.setMoveType(1);
//        stockMoveTheme.setCode("12323");
//        stockMoveTheme.setName("name");
//        stockMoveTheme.setStockTime(12L);
//        redisClient.lSet(entityListKey,stockMoveTheme);
//        List<Object> objects = redisClient.lGet(entityListKey, 0, 1);
//        stockMoveTheme.setName("name2");
//        stockMoveTheme.setStockTime(13L);
//        redisClient.lSet(entityListKey,stockMoveTheme);
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(10);
        Set<String> code = new HashSet<>();
//        code.add("1232");
//        Page<StockMove> stockMovePage = stockMoveApi.pageList(page, code, 1, "", -1L );
        Set<Integer> types = new HashSet<>();
//        types.add(1);
        String endTimeOrg = "1695607043000";
        PageWithCount<StockMove> stockMovePage2 = stockMoveApi.pageListByTypeListPc(page, code, types,1,endTimeOrg,-1);
        System.out.println(types);

    }

    public static void main(String[] args) {
        List<String> chineseList = new ArrayList<>();
        chineseList.add("张三");
        chineseList.add("李四");
        chineseList.add(null);


        Comparator collator = Comparator.nullsLast(Collator.getInstance(Locale.CHINA));
        Collections.sort(chineseList, collator);

        for (String name : chineseList) {
            System.out.println(name);
        }
    }

    private static String reg(String content) {
        if (content.contains("不适用")) {
            return "--";
        }
        String pattern = "\\([^)]*\\)";
        content = content.replaceAll(pattern, "");
        return content.replace("港元", "").trim();
    }

    @Test
    public void director() {
        ResultT<List<DirectorManager>> listResultT = f10StockInformationApi.directorManager("00700.hk");

        System.out.println(JSON.toJSONString(listResultT, SerializerFeature.WriteMapNullValue));

    }

    @Test
    public void securityInfo() {
        //    ResultT<SecuritiesInformation> tencent = f10StockInformationApi.securityInfo("00700.hk");
        //   System.out.println(JSON.toJSONString(tencent, SerializerFeature.WriteMapNullValue));
        long start = System.currentTimeMillis();
        ResultT<SecuritiesInformation> securitiesInformationResultT = f10StockInformationApi.securityInfo("06669.hk");
        System.out.println(JSON.toJSONString(securitiesInformationResultT, SerializerFeature.WriteMapNullValue));
        System.out.println("耗时：" + (System.currentTimeMillis() - start));

    }


    @Test
    public void testUts() {
        PageDomain<StockUtsNoticeListResp> stockUtsNoticeListRespPageDomain = utsInfoService.listNoticeByMongo(1, "00178.hk", 1, 20);
        System.out.println(JSON.toJSONString(stockUtsNoticeListRespPageDomain.getRecords()));
    }

    @Test
    public void checkReport() {
        f10TableTemplateApi.getReportType("00700.hk", 1, 1);
    }

    @Test
    public void getKey() {
        f10TableTemplateApi.getRatingsTable("00700.hk", 10);
    }


    @Test
    public void getFin() {
        F10CommonFinTable s = f10TableTemplateApi.getFinancialTable("00700.hk");
        System.out.println(s);
    }

    @Test
    public void getF10() {
        f10SourceService.getF10Table("00700.hk", "Q1", new Date().getTime(), 2);
    }

    @Test
    void getIndex() {
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode("00700.hk").build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(10)
                .currentPage(0)
                .build();
        List<F10CashFlowEntity> list = f10CashFlowDao.pageCashFlow(f10PageReq).getRecord();
        F10CashFlowEntity entity = list.get(0);
        Field[] fields = entity.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            System.out.println(fields[i].getName()); //属性名
        }
    }


    @Test
    void getF10Source() {
        f10SourceService.getF10Table("00700.hk", "Q1", 1368547200000L, 2);
    }

    @Test
    void getCheckProfit() {
        List<F10ProfitEntity> list = f10TableTemplateApi.getProfitChart("00700.hk", 10);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    void getF10PcSource() {
        List<List<F10TableTemplate>> pcf10Table = f10SourceService.getPCF10Table("00005.hk", 1, 4, 1, 10);
        System.out.println(JSON.toJSONString(pcf10Table));
    }

    @Test
    void getF10Ratings() {
        List<RatingsTableEntity> ratingsTable = f10SourceService.getRatingsTable("00700.hk", 10);
        System.out.println(JSON.toJSONString(ratingsTable));
    }

    @Test
    void delRedisKey() {
        Set<String> keys = redisClient.getKeys("/attachment/".concat("*"));
        redisClient.del(keys.toArray(new String[0]));
    }

    /**
     * 获取指定日期的所有派息的股票
     */
    @Test
    void queryDividendStock() {
        Long date = 20220906L;
        ResultT<List<Xnhk0127>> result = utsInfoService.queryDividendStock(date);
    }

    @Test
    public void getBrokerStatisticsTest() {

        BrokerStatisticsParams brokerStatisticsReq = new BrokerStatisticsParams();
        brokerStatisticsReq.setPeriod(0);
        brokerStatisticsReq.setSize(3);
        brokerStatisticsReq.setSymbol("00700.hk");
//        brokerStatisticsReq.setType("");

        ResultT<BrokerStatisticsRes> brokerStatisticsRespResultT = brokerInfoServiceApi.getBrokerStatistics(brokerStatisticsReq);
        List<BrokerDetailsRes> buy = brokerStatisticsRespResultT.getData().getBuy();

        List<BrokerDetailsRes> sell = brokerStatisticsRespResultT.getData().getSell();


        System.out.println(brokerStatisticsRespResultT);
    }

    @Test
    public void getLastSixTradingCalendars() {

        hkTradingCalendarApi.getLastSixTradingCalendars(LocalDate.parse("2022-10-01"),7);
    }
    @Test
    public void findEndTradeTempStock() {

        ResultT<List<ReuseTempDTO>> endTradeTempStock = utsInfoService.findEndTradeTempStock(DateUtils.addDays(new Date(),-7));
        System.out.println(endTradeTempStock);
    }
    @Test
    public void findTradingTempStockByTime() {

        List<ReuseTempDTO> endTradeTempStock = utsInfoService.findTradingTempStockByTime(DateUtils.addDays(new Date(),-7));
        System.out.println(endTradeTempStock);
    }

    @Test
    public void findAllTempStocks() {

        List<ReuseTempDTO> endTradeTempStock = utsInfoService.findAllTempStocks();
        System.out.println(endTradeTempStock);
    }

    @Test
    public void findAllUnTradingTempSocks() {
        List<String> endTradeTempStock = utsInfoService.findAllUnTradeTempSocks(DateUtil.date());
        System.out.println(endTradeTempStock);
    }

    @Test
    public void getXnhk0127History() {
        List<Xnhk0127> endTradeTempStock = utsInfoService.getXnhk0127History(DateUtil.date());
        System.out.println(endTradeTempStock);
    }

    @Test
    public void getXnhks0101sToday() {
        ResultT<List<String>> endTradeTempStock = iStockMarketService.getXnhks0101sToday();
        System.out.println(endTradeTempStock);
    }

    @Test
    public void updateCompanyTrends() {
        trendsJob.updateCompanyTrends("");
    }

    @Test
    public void getStockConversionMarket() {
        System.out.println(iStockMarketService.getStockConversionMarket("20230929"));
    }

    @Test
    public void updateStockMoveStockCode() {
        stockMoveApi.updateStockMoveStockCode("09690.hk","09690-t.hk");
    }

    @Test
    public void updateSceneDate() {
        System.out.println(stockSceneSimulateApi.updateSceneDate(Arrays.asList("01193-t.hk")));
    }

    @Test
    public void getAllStockRights() {
        System.out.println(utsInfoService.getAllStockRights());
    }

    @Test
    public void getTradingStockRights() {
        System.out.println(utsInfoService.getTradingStockRights(new Date()));
    }

    @Test
    public void getStartTradingStockRights() {
        System.out.println(utsInfoService.getStartTradingStockRights(DateUtils.parseDate("2024-09-12")));
    }

    @Test
    public void getEndTradingStockRights() {
        System.out.println(utsInfoService.getEndTradingStockRights(DateUtils.parseDate("2024-09-20")));
    }

    @Test
    public void getUnTradingStockRights() {
        System.out.println(utsInfoService.getUnTradingStockRights(DateUtils.parseDate("2024-10-20")));
    }

    @Test
    public void dataMonitor() {
        System.out.println(dataMonitorJob.dataMonitor(null));
    }

    @Test
    public void getStartTradingStockRightsMock() {
        System.out.println(stockSceneSimulateApi.getStartTradingStockRights(DateUtils.parseDate("2024-09-20")));
    }

    @Test
    public void getEndTradingStockRightsMock() {
        System.out.println(stockSceneSimulateApi.getEndTradingStockRights(DateUtils.parseDate("2024-09-24")));
    }

    @Test
    public void queryAfterTradingCalendars() {

        hkTradingCalendarApi.queryAfterTradingCalendars(LocalDate.parse("2025-01-22"),89);
    }

    @Test
    public void listNewShare4Check() {
        List<ComNewShareVo> re = informationApi.listNewShare4Check();
        System.out.println(JSON.toJSONString(re));
    }

}
