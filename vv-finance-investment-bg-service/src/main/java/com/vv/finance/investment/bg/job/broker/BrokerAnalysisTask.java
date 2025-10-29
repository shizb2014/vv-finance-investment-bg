package com.vv.finance.investment.bg.job.broker;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.esotericsoftware.minlog.Log;
import com.google.common.collect.Lists;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk0609;
import com.vv.finance.investment.bg.entity.uts.Xnhk0610;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0102Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0127Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0609Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0610Mapper;
import com.vv.finance.investment.bg.stock.info.BrokerIndustryStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerMarketValueStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerIndustryStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerMarketValueStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.utils.DateUtils;
import com.vv.finance.investment.bg.utils.JsonUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auther: shizhibiao
 * @Date: 2022/10/21
 * @Description: com.vv.finance.investment.job.handler.broker
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerAnalysisTask {

    @Autowired
    private BrokerAnalysisApi brokerAnalysisApi;

    @Autowired
    private StockService stockService;

    @Autowired
    private HkTradingCalendarApi hkTradingCalendarApi;

    @Autowired
    private Executor brokerExecutorService;

    @Resource
    Xnhk0127Mapper xnhk0127Mapper;

    @Resource
    Xnhk0102Mapper xnhk0102Mapper;

    @Autowired
    Xnhk0609Mapper xnhk0609Mapper;

    @Autowired
    BrokerMarketValueStatisticsMapper brokerMarketValueStatisticsMapper;

    @Resource
    BrokerStatisticsMapper brokerStatisticsMapper;

    @Resource
    IStockMarketService stockMarketService;

    @Autowired
    BrokerIndustryStatisticsMapper brokerIndustryStatisticsMapper;

    @Value("${broker.analysis.Limit:3}")
    private Integer BrokerAnalysisDataLimit;

    @Autowired
    RedisClient redisClient;

    @Resource
    private UtsInfoService utsInfoService;

    /**
     * 1、将经纪商id和名称关系维护一份到redis，使用时方便
     * 2、将行业和股票关系维护一份到redis，使用时方便
     * 3、每天执行一次
     * cron = "0 0 2 * * ?",
     *
     * @param param
     * @return
     */
    @XxlJob(value = "updateBrokerInfoJob", author = "史志彪", desc = "将经纪商基本数据缓存到redis(0 0 2 * * ?)")
    public ReturnT<String> updateBrokerInfoJob(String param) {
        log.info("缓存经纪商数据启动, {}", LocalDateTime.now());
        brokerAnalysisApi.updateBrokerInfoJob();
        log.info("缓存经纪商数据结束, {}", LocalDateTime.now());
        return ReturnT.SUCCESS;
    }

    /**
     * 每天晚上计算当天的数据落库
     * cron = "0 10 17 * * ?"
     *
     * @param param
     * @return
     */
    @XxlJob(value = "BrokerAnalysisCalculationTask", author = "史志彪", desc = "日终经纪商数据计算（0 10 17 * * ?）")
    public ReturnT<String> BrokerAnalysisCalculationTask(String param) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        log.info("日终经纪商数据计算启动, {}", param);
        if (StringUtils.isEmpty(param)) {
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
            endDate = LocalDate.now();
        }else{
            Map<String, String> paramsMap = JsonUtils.parseJsonToMapString(param);
            String startDateStr = paramsMap.get("startDate");
            String endDateStr = paramsMap.get("endDate");
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (StrUtil.isNotBlank(endDateStr)) {
                endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else {
                endDate = LocalDate.now();
            }
        }

        Set<String> codeLists = stockService.getAllStockCode();
        Set<String> stockRightsCodes = utsInfoService.getTradingStockRights(new Date()).stream().map(StockRightsDTO::getCode).collect(Collectors.toSet());
        Set<String> codes = codeLists.stream().filter(code -> !stockRightsCodes.contains(code)).collect(Collectors.toSet());
        List<BgTradingCalendar> tradingCalendars = hkTradingCalendarApi.getTradingCalendarByDate(startDate, endDate);
        for (BgTradingCalendar tradingCalendar : tradingCalendars) {
            LocalDate calcDate = tradingCalendar.getDate();
            //股权数据不需要落库
            List<CompletableFuture<Void>> all = Lists.newArrayList();
            for (String code : codes) {
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> brokerAnalysisApi.updateNetTradeBrokerJob(code, calcDate), brokerExecutorService);
                all.add(voidCompletableFuture);
                log.info("日终经纪商数据计算启动 code: {}", code);
            }
            CompletableFuture.allOf(all.stream().toArray(size -> new CompletableFuture[size])).join();

            //计算经纪商-行业维度数据
            List<String> quitCodeList = stockMarketService.getQuitCode().getData();
            List<String> codeList = codes.stream().filter(item -> !quitCodeList.contains(item)).collect(Collectors.toList());

            brokerAnalysisApi.insertBrokersMakValStatistics(Long.parseLong(calcDate.toString().replace("-", "")), codeList);
            brokerAnalysisApi.updateIndustryStatisticsBatch(DateUtils.localDateToF001D(calcDate), codeList);
        }
        log.info("日终经纪商数据计算结束, {}", LocalDateTime.now());

        return ReturnT.SUCCESS;
    }

    /**
     * 每天先删除当天的redis缓存，并且重新计算第二天的redis
     * 支持入参日期格式
     * cron = "0 30 17 * * ?"
     *
     * 1、删除redis缓存数据
     * 2、计算当天的redis数据
     * @param param
     * @return
     */
    @XxlJob(value = "BrokerAnalysisRedisTask", author = "史志彪", desc = "更新经纪商redis数据（0 30 1,17 * * ?）")
    public ReturnT<String> BrokerAnalysisRedisTask(String param) {

        //删除经纪商redis缓存
        redisClient.del(BrokerConstants.BROKER_TOP10CHANGETODAY);
        redisClient.del(BrokerConstants.BROKER_TOP10CONCENTRATION);
        redisClient.del(BrokerConstants.BROKER_TOP5CHANGETODAY);
        redisClient.del(BrokerConstants.BROKER_TOP5CONCENTRATION);
        redisClient.del(BrokerConstants.BROKER_TODAYQUANTITY);
        redisClient.del(BrokerConstants.BROKER_TODAYMARKETVAL);

        Set<String> keys1 = redisClient.getKeys(BrokerConstants.POSITION_STATISTICS_DETAIL.concat("*"));
        redisClient.del(keys1.toArray(new String[0]));
        Set<String> keys2 = redisClient.getKeys(BrokerConstants.BROKER_TOP5CHANGE.concat("*"));
        redisClient.del(keys2.toArray(new String[0]));
        Set<String> keys3 = redisClient.getKeys(BrokerConstants.BROKER_TOP10CHANGE.concat("*"));
        redisClient.del(keys3.toArray(new String[0]));
        Set<String> keys4 = redisClient.getKeys(BrokerConstants.BROKER_TOP5.concat("*"));
        redisClient.del(keys4.toArray(new String[0]));
        Set<String> keys5 = redisClient.getKeys(BrokerConstants.BROKER_5DAY.concat("*"));
        redisClient.del(keys5.toArray(new String[0]));
        Set<String> keys6 = redisClient.getKeys(BrokerConstants.BROKER_10DAY.concat("*"));
        redisClient.del(keys6.toArray(new String[0]));
        Set<String> keys7 = redisClient.getKeys(BrokerConstants.BROKER_20DAY.concat("*"));
        redisClient.del(keys7.toArray(new String[0]));
        Set<String> keys8 = redisClient.getKeys(BrokerConstants.BROKER_60DAY.concat("*"));
        redisClient.del(keys8.toArray(new String[0]));
        Set<String> keys9 = redisClient.getKeys(BrokerConstants.BROKER_360DAY.concat("*"));
        redisClient.del(keys9.toArray(new String[0]));
        Set<String> keys10 = redisClient.getKeys(BrokerConstants.BROKER_TODAY.concat("*"));
        redisClient.del(keys10.toArray(new String[0]));
        Set<String> keys11 = redisClient.getKeys(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD.concat("*"));
        redisClient.del(keys11.toArray(new String[0]));
        Set<String> keys12 = redisClient.getKeys(BrokerConstants.BROKER_HOLD_NUM_VALUE.concat("*"));
        redisClient.del(keys12.toArray(new String[0]));
        Set<String> keys13 = redisClient.getKeys(BrokerConstants.BROKER_MARKET_VALUE.concat("*"));
        redisClient.del(keys13.toArray(new String[0]));
        Set<String> keys14 = redisClient.getKeys(BrokerConstants.BROKER_CODE_HOLD_NUM_VALUE.concat("*"));
        redisClient.del(keys14.toArray(new String[0]));
        Set<String> keys15 = redisClient.getKeys(BrokerConstants.BROKER_CODE_MARKET_VALUE.concat("*"));
        redisClient.del(keys15.toArray(new String[0]));
        Set<String> keys16 = redisClient.getKeys(BrokerConstants.BROKER_VALUE.concat("*"));
        redisClient.del(keys16.toArray(new String[0]));
        Set<String> keys17 = redisClient.getKeys(BrokerConstants.BROKER_VALUE_LIST.concat("*"));
        redisClient.del(keys17.toArray(new String[0]));

        LocalDate endDate;
        if (StringUtils.isNotEmpty(param)) {
            endDate = LocalDate.parse(param, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }else{
            endDate = LocalDate.now();
        }
        log.info("日终更新经纪商redis数据启动, {}", LocalDateTime.now());
        Set<String> codes = stockService.getAllStockCode();
        List<CompletableFuture<Void>> all = Lists.newArrayList();
        for (String code : codes) {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> brokerAnalysisApi.updateRedisBrokerJob(code, endDate), brokerExecutorService);
            all.add(voidCompletableFuture);
            log.info("更新经纪商redis数据 code: {}", code);
        }
        CompletableFuture.allOf(all.stream().toArray(size -> new CompletableFuture[size])).join();
        log.info("更新经纪商redis数据结束, {}", LocalDateTime.now());
        //计算市值
        return ReturnT.SUCCESS;
    }

    /**
     * 实时获取净买卖数据，每五分钟执行一次
     * cron = "0 0/5 * ? * *"
     *
     * @param param
     * @return
     */
    @XxlJob(value = "BrokerAnalysisRealTimeTask", author = "史志彪", desc = "实时获取经纪商数据缓存(0 */5 * ? * *)")
    public ReturnT<String> BrokerAnalysisRealTimeTask(String param) {
        long time1 = System.currentTimeMillis();
        log.info("实时获取经纪商数据缓存启动, {}", LocalDateTime.now());
        Set<String> codes = stockService.getAllStockCode();
//        List<CompletableFuture<Void>> all = Lists.newArrayList();
        List<Xnhk0102> xnhk0102s = xnhk0102Mapper.selectList(null);
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102s.stream().collect(Collectors.toMap(Xnhk0102::getSeccode, Function.identity()));
        for (String code : codes) {
            brokerAnalysisApi.updateBrokerStatisticsJob(code,xnhk0102Map.get(code));
            log.info("实时获取经纪商数据 code: {}", code);
        }
        log.info("实时获取经纪商数据耗时, {}", System.currentTimeMillis() - time1);
        //更新缓存时间
        brokerAnalysisApi.updateBrokerDatetime();
        return ReturnT.SUCCESS;
    }

    /**
     * 1、同步融聚汇数据（今天同步上一天的）该job计算了市值和收盘价 执行时间 （0 0 17 * * ?）
     * 2、可传入参手动执行
     * 入参格式 {"startDate":"20221102", "endDate":"20221103"}
     * @param param
     * @return
     */
    @XxlJob(value = "updateNetTradeBrokerForDateJob", author = "史志彪", desc = "同步融聚汇数据（0 0 17 * * ?）")
    public ReturnT<String> updateNetTradeBrokerForDateJob(String param) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        log.info("指定日期区间数据落库启动, {}", param);
        if (StringUtils.isEmpty(param)) {
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
            endDate = LocalDate.now();
        }else{
            Map<String, String> paramsMap = JsonUtils.parseJsonToMapString(param);
            String startDateStr = paramsMap.get("startDate");
            String endDateStr = paramsMap.get("endDate");
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        List<Xnhk0102> xnhk0102s = xnhk0102Mapper.selectList(new QueryWrapper<Xnhk0102>().select("seccode","f070n"));
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102s.stream().collect(Collectors.toMap(Xnhk0102::getSeccode, Function.identity()));

        List<BgTradingCalendar> bg = hkTradingCalendarApi.queryTradingCalendarsBySection(startDate, endDate);
        //过滤退市股票
        Set<String> codes = stockService.getAllStockCode();
        List<String> quitCodeList = stockMarketService.getQuitCode().getData();
        List<String> codeList = codes.stream().filter(item -> !quitCodeList.contains(item)).collect(Collectors.toList());
        for (BgTradingCalendar bgTradingCalendar : bg) {
            List<Xnhk0609> xnhk0609s = xnhk0609Mapper.selectList(new QueryWrapper<Xnhk0609>()
                    .eq("f001d", Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", "")))
                    .last("limit 1"));
            if(CollectionUtils.isEmpty(xnhk0609s)){
                log.info("指定日期:{}融聚汇缺失数据，不处理该天数据", Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", "")));
                continue;
            }
            log.info("指定日期数据落库：{}", bgTradingCalendar.getDate());
            List<CompletableFuture<Void>> all = Lists.newArrayList();
            for (String code : codes) {
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> brokerAnalysisApi.updateNetTradeBrokerForDateJob(code, Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", "")), xnhk0102Map), brokerExecutorService);
                all.add(voidCompletableFuture);
            }
            CompletableFuture.allOf(all.stream().toArray(size->new CompletableFuture[size])).join();
        }
        long time1 = System.currentTimeMillis();
        List<CompletableFuture<Void>> all1 = Lists.newArrayList();
        for (BgTradingCalendar bgTradingCalendar : bg) {
            log.info("计算经纪商维度市值数据 date: {}", bgTradingCalendar.getDate());
            CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> brokerAnalysisApi.insertBrokersMakValStatistics(Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", "")), codeList), brokerExecutorService);
            all1.add(voidCompletableFuture1);
        }
        CompletableFuture.allOf(all1.stream().toArray(size->new CompletableFuture[size])).join();
        log.info("================计算经纪商维度市值数据结束======耗时1 :{}===", System.currentTimeMillis() - time1);

        long time2 = System.currentTimeMillis();
        for (BgTradingCalendar bgTradingCalendar : bg) {
            Long date = Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", ""));
            log.info("计算经纪商-行业维度市值数据 date: {}", bgTradingCalendar.getDate());
            brokerAnalysisApi.updateIndustryStatisticsBatch(date, codeList);
        }
        log.info("================计算经纪商-行业维度市值数据结束======耗时1 :{}===", System.currentTimeMillis() - time2);

        return ReturnT.SUCCESS;
    }

    /**
     * 计算拆并股
     * 1、计算自建表中所有日期的持有量，行情
     * 2、重新计算 经纪商维度市值
     */
    @XxlJob(value = "updateBrokerStockEventJob", author = "史志彪", desc = "计算拆并股(0 15 17 * * ?)")
    public ReturnT<String> updateBrokerStockEventJob(String param) {

        List<Xnhk0127> xnhk0127s = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>()
                .eq("F003D", LocalDate.now())
                .in("F002V", Arrays.asList("SS", "SC")));

        if (ObjectUtils.isNotEmpty(xnhk0127s)) {
            LocalDate today = LocalDate.now();
            LocalDate startDateBg = today.minusMonths(18);

            //计算市值
            for (Xnhk0127 xnhk0127 : xnhk0127s) {
                log.info("计算拆并股影响经纪商维度的数据code:{}", xnhk0127.getSeccode());
                List<BgTradingCalendar> bgs = hkTradingCalendarApi.queryTradingCalendarsBySection(startDateBg, xnhk0127.getF003d().plusDays(1));
                List<Long> dates = bgs.stream().map(bgTradingCalendar->Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", ""))).collect(Collectors.toList());

                long time  = System.currentTimeMillis();
                brokerAnalysisApi.updateBrokerStockEventV2(xnhk0127.getSeccode(), xnhk0127.getF004n(), dates);
                long time1  = System.currentTimeMillis();
                log.info("计算609前复权耗时：{}ms", time1 - time);

                brokerAnalysisApi.updateBrokerInfoEventV2(xnhk0127.getSeccode(), xnhk0127.getF004n(), dates);
                long time2  = System.currentTimeMillis();
                log.info("计算608前复权耗时：{}ms", time2 - time1);
            }

            //计算经纪商-行业维度数据
            List<BgTradingCalendar> bg = hkTradingCalendarApi.queryTradingCalendarsBySection(startDateBg,LocalDate.now().plusDays(1));
            List<Long> dates = bg.stream().map(bgTradingCalendar->Long.parseLong(bgTradingCalendar.getDate().toString().replace("-", ""))).collect(Collectors.toList());

            //过滤退市股票
            Set<String> codes = stockService.getAllStockCode();
            List<String> quitCodeList = stockMarketService.getQuitCode().getData();
            List<String> codeList = codes.stream().filter(item -> !quitCodeList.contains(item)).collect(Collectors.toList());

            long time6  = System.currentTimeMillis();
            for (Long date : dates) {

                //所有日期循环
                log.info("拆并股后计算经纪商-维度数据,日期:{}", date);
                brokerAnalysisApi.insertBrokersMakValStatistics(date, codeList);
                brokerAnalysisApi.updateIndustryStatisticsBatch(date, codeList);
            }
            long time7  = System.currentTimeMillis();
            log.info("计算经纪商-维度数据耗时：{}ms", time7-time6);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 删除两年前的数据
     * 支持入参，时间长度可配置
     */
    @XxlJob(value = "deleteHisBrokerStock", author = "史志彪", desc = "删除历史数据(0 0 2 * * ?)")
    public ReturnT<String> deleteHisBrokerStock(String param) {

        LocalDate today = LocalDate.now();
        LocalDate previousYear = today.minus(BrokerAnalysisDataLimit, ChronoUnit.MONTHS);
        Log.info("Date before 2 year : {}" + previousYear);

        Set<String> codes = stockService.getAllStockCode();
        List<CompletableFuture<Void>> all = Lists.newArrayList();
        for (String code : codes) {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> brokerAnalysisApi.deleteHisBrokerStock(code, Long.parseLong(previousYear.toString().replace("-", ""))), brokerExecutorService);
            all.add(voidCompletableFuture);
            log.info("删除两年前的数据 code: {}", code);
        }
        CompletableFuture.allOf(all.stream().toArray(size->new CompletableFuture[size])).join();

        brokerAnalysisApi.deleteHisBrokerStockMarket(Long.parseLong(previousYear.toString().replace("-", "")));

        return ReturnT.SUCCESS;
    }
}
