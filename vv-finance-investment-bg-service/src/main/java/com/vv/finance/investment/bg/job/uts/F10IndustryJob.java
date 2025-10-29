//package com.vv.finance.investment.bg.job.uts;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateUtil;
//import com.alibaba.fastjson.JSON;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.vv.finance.common.constants.RedisKeyConstants;
//import com.vv.finance.common.entity.common.StockSnapshot;
//import com.vv.finance.common.utils.DateUtils;
//import com.vv.finance.common.utils.ZipUtil;
//import com.vv.finance.hk.quotation.common.stock.dto.StockKlineReq;
//import com.vv.finance.hk.quotation.common.stock.entity.KlineEntity;
//import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
//import com.vv.finance.investment.bg.cache.StockCache;
//import com.vv.finance.investment.bg.config.RedisClient;
//import com.vv.finance.investment.bg.entity.BgTradingCalendar;
//import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
//import com.vv.finance.investment.bg.entity.uts.Xnhk0403;
//import com.vv.finance.investment.bg.mapper.uts.Xnhk0403Mapper;
//import com.vv.finance.investment.composite.api.HkStockCompositeApi;
//import com.vv.finance.investment.rank.api.IndustryApi;
//import com.vv.finance.investment.rank.dto.IndustryKline;
//import com.vv.finance.investment.rank.dto.IndustryKlineReq;
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.common.utils.CollectionUtils;
//import org.apache.dubbo.config.annotation.DubboReference;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDate;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @ClassName F10IndustryJob
// * @Deacription 行业对比job
// * @Author lh.sz
// * @Date 2021年08月23日 10:35
// **/
//@Component
//@Slf4j
//public class F10IndustryJob {
//
//    @Resource
//    Xnhk0403Mapper xnhk0403Mapper;
//    @Resource
//    RedisClient redisClient;
//    @Resource
//    MongoTemplate mongoTemplate;
//
//    @DubboReference(group = "${dubbo.rank.group:rank}", registry = "rankservice")
//    IndustryApi industryApi;
//    @DubboReference(group = "${dubbo.investment.composite.service.group:composite}", registry = "compositeservice")
//    HkStockCompositeApi hkStockCompositeApi;
//    @DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
//    HkTradingCalendarApi tradingCalendarApi;
//    @Resource
//    private StockCache stockCache;
//
//
//
//    private static final String INDEX_CODE = "0000100";
//
//    private static final String INDEX_NAME = "恒生指数";
//
//
//
//    @XxlJob(value = "syncMarket", author = "罗浩", desc = "F10市场表现对比保存", cron = "0 30 8 ? * 2,3,4,5,6 *")
//    public ReturnT<String> syncMarket(String param) {
//        if (tradingCalendarApi.isTradingDay(LocalDate.now())) {
//            //获取当前日期的上一个交易日
//            Date date = DateUtils.localDate2Date(tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate());
//            //保存恒生指数市场行业信息
//            saveMarketIndex(date);
//            //保存市场行业信息
//            saveMarketIndustry(date);
//            //保存市场对比股票数据
//            saveMarketStock(date);
//            log.info("保存所有市场类型成功");
//        }
//        return ReturnT.SUCCESS;
//    }
//
//
//
//
//    /**
//     * 保存市场行业信息
//     */
//    public void saveMarketIndustry(Date date) {
//        //行业涨跌幅
//        Map<String, List<IndustryKline>> map = industryApi.industryKlineReq(IndustryKlineReq.builder().date(DateUtils.date2LocalDate(date)).build());
//        Date nearWeekDay = DateUtils.localDate2Date(tradingCalendarApi.queryLastTradingCalendars(5).stream()
//                .reduce((first, second) -> second).orElse(new BgTradingCalendar()).getDate());
//        Date fiveDay = DateUtils.getDate(date, -5, Calendar.DAY_OF_MONTH);
//        Date nearMonthDay = DateUtils.getDate(date, -1, Calendar.MONTH);
//        Date nearThreeMonthDay = DateUtils.getDate(date, -3, Calendar.MONTH);
//        Date nearSixMonthDay = DateUtils.getDate(date, -6, Calendar.MONTH);
//        Date nearFiftyTwoWeeksDay = DateUtils.getDate(date, -52, Calendar.DAY_OF_WEEK);
//        Date yearToDate = DateUtil.beginOfYear(date);
//        Date nearOneYearDay = DateUtils.getDate(date, -1, Calendar.YEAR);
//        Date nearTwoYearDay = DateUtils.getDate(date, -2, Calendar.YEAR);
//        Date nearThreeYearDay = DateUtils.getDate(date, -3, Calendar.YEAR);
//        map.forEach((k, v) -> {
//            BigDecimal last = v.get(0).getLast();
//            MarketPresence marketPresence = MarketPresence.builder().build();
//            marketPresence.setCode(k);
//            marketPresence.setName(v.get(0).getName());
//            marketPresence.setTodayChgPct(v.get(0).getChgPct());
//            marketPresence.setFiveDayChgPct(getIndustryChgPct(last, fiveDay, map, k));
//            marketPresence.setWeekChgPct(getIndustryChgPct(last, nearWeekDay, map, k));
//            marketPresence.setMonthChgPct(getIndustryChgPct(last, nearMonthDay, map, k));
//            marketPresence.setNearThreeMonthChgPct(getIndustryChgPct(last, nearThreeMonthDay, map, k));
//            marketPresence.setNearSixMonthChgPct(getIndustryChgPct(last, nearSixMonthDay, map, k));
//            marketPresence.setFiftyTwoWeeksChgPct(getIndustryChgPct(last, nearFiftyTwoWeeksDay, map, k));
//            marketPresence.setYearToDateChgPct(getIndustryChgPct(last, yearToDate, map, k));
//            marketPresence.setNearOneYearChgPct(getIndustryChgPct(last, nearOneYearDay, map, k));
//            marketPresence.setNearTwoYearChgPct(getIndustryChgPct(last, nearTwoYearDay, map, k));
//            marketPresence.setNearThreeYearChgPct(getIndustryChgPct(last, nearThreeYearDay, map, k));
//            marketPresence.setTime(date.getTime());
//            marketPresence.setType(3);
//            marketPresence.setCreateTime(date);
//            marketPresence.setUpdateTime(date);
//            marketPresence.setStrTime(DateUtils.formatDate(date, PATTERN));
//            mongoTemplate.save(marketPresence, COLLECTION_NAME);
//        });
//    }
//
//    /**
//     * 保存恒生指数市场行业信息
//     */
//    public void saveMarketIndex(Date date) {
//        //获取最新一条日K数据
//        List<KlineEntity> klineEntityList = getIndexKline(1, date);
//        MarketPresence marketPresence = MarketPresence.builder().build();
//        marketPresence.setCode(INDEX_CODE);
//        marketPresence.setName(INDEX_NAME);
//        marketPresence.setTodayChgPct(klineEntityList.get(0).getChgPct());
//
//        Date nearWeekDay = DateUtils.getLastDateByType(date, -1, Calendar.WEEK_OF_YEAR);
//        Date nearMonthDay = DateUtils.getLastDateByType(date, -1, Calendar.MONTH);
//        Date nearThreeMonthDay = DateUtils.getLastDateByType(date, -3, Calendar.MONTH);
//        Date nearSixMonthDay = DateUtils.getLastDateByType(date, -6, Calendar.MONTH);
//        Date nearFiftyTwoWeeksDay = DateUtils.getLastDateByType(date, -52, Calendar.WEEK_OF_YEAR);
//        Date nearYearDay = DateUtils.getLastDateByType(date, -1, Calendar.YEAR);
//        Date nearTwoYearDay = DateUtils.getLastDateByType(date, -2, Calendar.YEAR);
//        Date nearThreeYearDay = DateUtils.getLastDateByType(date, -3, Calendar.YEAR);
//
//        marketPresence.setWeekChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearWeekDay));
//        marketPresence.setMonthChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearMonthDay));
//        marketPresence.setNearThreeMonthChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearThreeMonthDay));
//        marketPresence.setNearSixMonthChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearSixMonthDay));
//        marketPresence.setFiftyTwoWeeksChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearFiftyTwoWeeksDay));
//        marketPresence.setYearToDateChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearYearDay));
//        marketPresence.setNearTwoYearChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearTwoYearDay));
//        marketPresence.setNearThreeYearChgPct(getIndexChgPct(klineEntityList.get(0).getClose(), nearThreeYearDay));
//
//        marketPresence.setTime(date.getTime());
//        marketPresence.setType(2);
//        marketPresence.setCreateTime(date);
//        marketPresence.setUpdateTime(date);
//        marketPresence.setStrTime(DateUtils.formatDate(date, PATTERN));
//        mongoTemplate.save(marketPresence, COLLECTION_NAME);
//    }
//
//    /**
//     * 获取指数的涨跌幅
//     *
//     * @param last 最新价 收盘价
//     * @param date 时间
//     * @return
//     */
//    private BigDecimal getIndexChgPct(BigDecimal last, Date date) {
//        if (!tradingCalendarApi.isTradingDay(DateUtils.date2LocalDate(date))) {
//            date = DateUtils.localDate2Date(tradingCalendarApi.getBeforeTradingCalendar(DateUtils.date2LocalDate(date)).getDate());
//            return calcChgPct(last, getIndexKline(1, date).stream().reduce((first, second) -> second).orElse(new KlineEntity()).getClose());
//        }
//        return calcChgPct(last, getIndexKline(1, date).stream().reduce((first, second) -> second).orElse(new KlineEntity()).getClose());
//    }
//
//
//    /**
//     * 获取行业的涨跌幅
//     *
//     * @param last 最新价
//     * @param date 时间
//     * @return
//     */
//    private BigDecimal getIndustryChgPct(BigDecimal last,
//                                         Date date,
//                                         Map<String, List<IndustryKline>> map,
//                                         String code) {
//        List<IndustryKline> industryKlineList;
//        if (!tradingCalendarApi.isTradingDay(DateUtils.date2LocalDate(date))) {
//            date = DateUtils.localDate2Date(tradingCalendarApi.getBeforeTradingCalendar
//                    (DateUtils.date2LocalDate(date)).getDate());
//        }
//        Map<String, List<IndustryKline>> listMap = industryApi.industryKlineReq(IndustryKlineReq.builder()
//                .date(DateUtils.date2LocalDate(date))
//                .num(1)
//                .build());
//        if (CollectionUtils.isEmptyMap(listMap)) {
//            industryKlineList = map.get(code);
//        } else {
//            industryKlineList = listMap.get(code);
//        }
//        if (CollUtil.isEmpty(industryKlineList)) {
//            return null;
//        }
//        return calcChgPct(last, industryKlineList.get(0).getPreClose());
//
//    }
//
//    /**
//     * 获取恒生指数的k线
//     *
//     * @param num  数量
//     * @param date 时间
//     * @return
//     */
//    private List<KlineEntity> getIndexKline(int num, Date date) {
//        StockKlineReq req = new StockKlineReq();
//        req.setType("day");
//        req.setTime(date.getTime());
//        req.setNum(num);
//        req.setCode(INDEX_CODE);
//        req.setAdjhkt("not");
//        return hkStockCompositeApi.selectKlineList(req);
//    }
//
//
//    /**
//     * 计算涨跌幅
//     *
//     * @param last     最新价
//     * @param preClose 昨收价
//     * @return
//     */
//    private BigDecimal calcChgPct(BigDecimal last,
//                                  BigDecimal preClose) {
//        if (last == null || preClose == null) {
//            return BigDecimal.ZERO;
//        }
//        if (last.compareTo(BigDecimal.ZERO) == 0 || preClose.compareTo(BigDecimal.ZERO) == 0) {
//            return BigDecimal.ZERO;
//        }
//        return last.subtract(preClose).divide(preClose, 4, RoundingMode.HALF_UP).
//                multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
//    }
//
//}
