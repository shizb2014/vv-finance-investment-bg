package com.vv.finance.investment.bg.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.TradingStateResp;
import com.vv.finance.investment.bg.entity.uts.HkexTd;
import com.vv.finance.investment.bg.stock.info.TradeCalendar;
import com.vv.finance.investment.bg.stock.trade.mapper.TradeCalendarMapper;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import com.vv.finance.investment.gateway.dto.req.HkTradingSessionStatusReq;
import com.vv.finance.investment.gateway.dto.resp.HkTradingSessionStatusResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/6/16 11:23
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class HkTradingCalendarApiImpl implements HkTradingCalendarApi {


    private final TradeCalendarMapper tradeCalendarMapper;
    private final RedisClient redisClient;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi iStockBusinessApi;

    private static final String CALENDAR_KEY = "BG:HkTradingCalendar:";
    @Value("${bg.HkTradingCalendar.expire.time:7200}")
    private Integer expireTime;
    private static final LocalTime am9 = LocalTime.of(9, 30);
    private static final LocalTime pm4 = LocalTime.of(16, 8);
    private static final LocalTime am12 = LocalTime.of(12, 0);
    private static final LocalTime pm400 = LocalTime.of(16, 0);
    private static final LocalTime am00 = LocalTime.of(00, 1);

    //上午延迟开市交易状态
    public final static String TRADING_CLOSE_AM = "10-102";
    //下午延迟开市交易状态
    public final static String TRADING_CLOSE_PM = "100-0";

    @Override
    public Boolean isTradingDay(LocalDate date) {
        if (null == date) {
            log.info("=====查询交易日历 isTradingDay 传参 date 为 null ====");
            return null;
        }
        BgTradingCalendar tradingCalendar = getTradingCalendar(date);
        if (ObjectUtils.isEmpty(tradingCalendar)) {
            log.info("=============isTradingDay 查不到交易日历 :{} ", date);
            return null;
        }
        return tradingCalendar.getTradingDay() != 0;
    }

    @Override
    public Boolean isTradingTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            log.info("=====查询交易日历 isTradingTime 传参 localDateTime 为 null ==== ");
            return null;
        }
        BgTradingCalendar tradingCalendar = this.getTradingCalendar(localDateTime.toLocalDate());
        if (ObjectUtils.isEmpty(tradingCalendar)) {
            log.info("=============isTradingTime查不到交易日历 :{} ", localDateTime);
            return null;
        }
        if (tradingCalendar.getTradingDay() == 0) {
            return false;
        }
        LocalTime localTime = localDateTime.toLocalTime();
        if (tradingCalendar.getTradingDay() == 1) {
            return localTime.isAfter(am9) && localTime.isBefore(pm4);
        }
        if (tradingCalendar.getTradingDay() == 2) {
            return localTime.isAfter(am9) && localTime.isBefore(am12);
        }
        if (tradingCalendar.getTradingDay() == 3) {
            return localTime.isAfter(am12) && localTime.isBefore(pm4);
        }
        return false;
    }


    @Override
    public BgTradingCalendar getTradingCalendar(LocalDate date) {
        if (null == date) {
            log.info("=====查询交易日历 getTradingCalendar 传参 data 为 null==== ");
            return null;
        }

//        log.info("=============交易日历 :{}", date);
        String dateLong = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String calendarKey = CALENDAR_KEY.concat(dateLong);
        String tradingCalendarStr = redisClient.get(calendarKey);
//        log.info("=============交易日历缓存 :{}", tradingCalendarStr);
        if (StringUtils.isNotEmpty(tradingCalendarStr)) {
            return JSON.parseObject(tradingCalendarStr, BgTradingCalendar.class);
        }
        TradeCalendar hkexTd = tradeCalendarMapper.selectOne(new QueryWrapper<TradeCalendar>().eq("rdate", dateLong));
//        log.info("=============交易日历表数据 :{}", hkexTd);
        if (ObjectUtils.isEmpty(hkexTd)) {
            log.info("=============getTradingCalendar查不到交易日历 :{} ", date);
            return null;
        }
        BgTradingCalendar tradingCalendar = build(hkexTd);
        redisClient.set(calendarKey, JSON.toJSONString(tradingCalendar), expireTime);
        return tradingCalendar;

    }

    @Override
    public List<BgTradingCalendar> getTradingCalendarList(List<String> dateList) {
        if (CollUtil.isEmpty(dateList)) {
            log.info("=====查询交易日历 getTradingCalendarList 传参 dateList 为 null==== ");
            return Collections.emptyList();
        }

        List<String> ymdList = dateList.stream().map(date -> DateUtil.format(DateUtil.parse(date), DatePattern.PURE_DATE_FORMAT)).collect(Collectors.toList());
        List<String> keyList = ymdList.stream().map(CALENDAR_KEY::concat).collect(Collectors.toList());
        List<String> calendarStrList = CollUtil.defaultIfEmpty(redisClient.batchGet(keyList), new ArrayList<>(CollUtil.size(dateList)));
        List<BgTradingCalendar> calendarList = calendarStrList.stream().map(str -> JSON.parseObject(str, BgTradingCalendar.class)).collect(Collectors.toList());

        // redis中的记录
        List<String> redisDayList = calendarList.stream().map(cd -> LocalDateTimeUtil.format(cd.getDate(), DatePattern.PURE_DATE_PATTERN)).collect(Collectors.toList());
        List<String> subtractList = CollUtil.subtractToList(ymdList, redisDayList);

        if (CollUtil.isNotEmpty(subtractList)) {
            log.info("=====查询交易日历 getTradingCalendarList 传参 subtractList: {}", subtractList);
            List<TradeCalendar> rdateList = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().in("rdate", subtractList));
            List<BgTradingCalendar> bgTradingCalendars = rdateList.stream().map(this::build).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(bgTradingCalendars)) {
                log.info("=====查询交易日历 getTradingCalendarList 传参 bgTradingCalendars: {}", bgTradingCalendars);
                Map<String, String> calendarMap = bgTradingCalendars.stream().collect(Collectors.toMap(cd ->
                        CALENDAR_KEY.concat(LocalDateTimeUtil.format(cd.getDate(), DatePattern.PURE_DATE_PATTERN)), JSON::toJSONString, (o, v) -> v));
                redisClient.strSetPipelined(calendarMap, expireTime);
                calendarList.addAll(bgTradingCalendars);
            }
        }

        return calendarList;
    }

    @Override
    public List<HkexTd> getIncrementTradingCalendar(
            Long beginTime,
            Long endTime
    ) {
        List<TradeCalendar> tradeCalendars = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime))));
        return BeanUtil.copyToList(tradeCalendars,HkexTd.class);
    }

    @Override
    public BgTradingCalendar getNextTradingCalendar(LocalDate date) {
        for (; ; ) {
            log.info("=============getNextTradingCalendar获取下一个交易日历 :{}", date);
            date = date.plusDays(1);
            BgTradingCalendar tradingCalendar = this.getTradingCalendar(date);

            if (ObjectUtils.isEmpty(tradingCalendar)) {
                log.info("=============getNextTradingCalendar查不到交易日历 :{} ", date);
                return null;
            }
            if (tradingCalendar.getTradingDay() != 0) {
                return tradingCalendar;
            }
        }

    }

    @Override
    public BgTradingCalendar getBeforeTradingCalendar(LocalDate date) {
        for (; ; ) {
            date = date.plusDays(-1);
            log.info("=============getBeforeTradingCalendar获取上一个交易日历 :{}", date);
            BgTradingCalendar tradingCalendar = this.getTradingCalendar(date);
            if (ObjectUtils.isEmpty(tradingCalendar)) {
                log.info("=============getBeforeTradingCalendar查不到交易日历 :{} ", date);
                return null;
            }
            if (tradingCalendar.getTradingDay() != 0) {
                return tradingCalendar;
            }
        }
    }

    @Override
    public List<BgTradingCalendar> queryLastTradingCalendars(Integer num) {
        long dateLong = Long.parseLong(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                le("rdate", dateLong).orderByDesc("rdate").last("limit " + num));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }

    @Override
    public List<BgTradingCalendar> queryGreaterTradingCalendars(Integer num) {
        long dateLong = Long.parseLong(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N")
                .ge("rdate", dateLong)
                .orderByAsc("rdate").last("limit " + num));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }


    @Override
    public ResultT<List<BgTradingCalendar>> getLastTradingCalendars(LocalDate date, Integer num) {
        long dateLong = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                le("rdate", dateLong).orderByDesc("rdate").last("limit " + num));
        return ResultT.success(hkexTds.stream().map(this::build).collect(Collectors.toList()));
    }

    @Override
    public ResultT<List<BgTradingCalendar>> getLastTradingCalendarsLtDate(LocalDate date, Integer num) {
        long dateLong = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                lt("rdate", dateLong).orderByDesc("rdate").last("limit " + num));
        return ResultT.success(hkexTds.stream().map(this::build).collect(Collectors.toList()));
    }

    @Override
    public List<BgTradingCalendar> queryFutureTradingCalendars(LocalDate date, Integer count) {
        long dateLong = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                ge("rdate", dateLong).orderByAsc("rdate").last("limit " + count));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }

    private Long parseDate(LocalDate date) {

        return date == null ? null : Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

    }

    private BgTradingCalendar build(TradeCalendar hkexTd) {
        String istrade = hkexTd.getIstrade().toUpperCase();
        Long rdate = hkexTd.getRdate();
        LocalDate date = LocalDate.parse(rdate + "", DateTimeFormatter.ofPattern("yyyyMMdd"));
        BgTradingCalendar tradingCalendar = BgTradingCalendar.builder().tradingDay(1).date(date).build();
        switch (istrade) {
            case "N":
                tradingCalendar.setTradingDay(0);
                break;
            case "H":
                tradingCalendar.setTradingDay(2);
                break;
            default:
                tradingCalendar.setTradingDay(1);

        }
        return tradingCalendar;
    }


    @Override
    public Integer getTradingDays(LocalDate startDate, LocalDate endDate) {
        Integer dayCount = tradeCalendarMapper.selectCount(new QueryWrapper<TradeCalendar>()
                .ge("rdate", parseDate(startDate))
                .le("rdate", parseDate(endDate))
                .ne("istrade", "N")
        );
        return dayCount;
    }

    @Override
    public ResultT<Integer> getBetweenTradingDays(LocalDate startDate, LocalDate endDate) {
        Integer dayCount = tradeCalendarMapper.selectCount(new QueryWrapper<TradeCalendar>()
                .ge("rdate", parseDate(startDate))
                .le("rdate", parseDate(endDate))
                .ne("istrade", "N")
        );
        if (dayCount > 0) {
            return ResultT.success(dayCount);
        }

        Integer totalCount = tradeCalendarMapper.selectCount(new QueryWrapper<TradeCalendar>()
                .ge("rdate", parseDate(startDate))
                .le("rdate", parseDate(endDate))
        );
        if (totalCount == 0) {
            return ResultT.fail("startDate endDate 时间段内没数据", "startDate endDate 时间段内没数据");
        }
        return ResultT.success(dayCount);
    }


    /**
     * 获取持仓天数
     *
     * @param startSureDate 建仓时间
     * @param queryDate     查询时间
     * @return 持仓天数
     */
    @Override
    public Integer queryTradingCountsBetweenDates(LocalDate startSureDate, LocalDate queryDate) {
        return tradeCalendarMapper.selectCount(new QueryWrapper<TradeCalendar>().orderByDesc("rdate").ne("istrade", "N").le(queryDate != null, "rdate", parseDate(queryDate)).ge(startSureDate != null, "rdate", parseDate(startSureDate)));
    }

    @Override
    public Long nextTradDateSecond() {
        LocalDate date = LocalDate.now();
        long plus = 0;
        for (; ; ) {
            plus++;
            if (isTradingDay(date.plusDays(plus))) {
                return plus * 86400;
            }
        }
    }

    @Override
    public Boolean isTradingTimeUntilFour(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            log.info("=====查询交易日历 isTradingTimeUntilFour 传参 localDateTime 为 null==== ");
            return null;
        }
        BgTradingCalendar tradingCalendar = this.getTradingCalendar(localDateTime.toLocalDate());
        if (ObjectUtils.isEmpty(tradingCalendar)) {
            log.info("=============isTradingTimeUntilFour查不到交易日历 :{} ", localDateTime.toLocalDate());
            return null;
        }
        if (tradingCalendar.getTradingDay() == 0) {
            return false;
        }
        LocalTime localTime = localDateTime.toLocalTime();
        if (tradingCalendar.getTradingDay() == 1) {
            return localTime.isAfter(am9) && localTime.isBefore(pm400);
        }
        if (tradingCalendar.getTradingDay() == 2) {
            return localTime.isAfter(am9) && localTime.isBefore(am12);
        }
        if (tradingCalendar.getTradingDay() == 3) {
            return localTime.isAfter(am12) && localTime.isBefore(pm400);
        }
        return false;
    }


    @Override
    public HkexTd queryBeginTradingCalendars(Integer num) {
        long dateLong = Long.parseLong(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                le("rdate", dateLong).orderByDesc("rdate").last("limit " + num));
        int index = Math.min(num - 1, hkexTds.size() - 1);
        TradeCalendar tradeCalendar = hkexTds.get(index);
        return BeanUtil.toBean(tradeCalendar,HkexTd.class);
    }

    @Override
    public LocalDate queryAfterTradingCalendars(LocalDate localDate, Integer count) {
        long dateLong = Long.parseLong(localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                gt("rdate", dateLong).orderByAsc("rdate").last("limit " + count));
        int smaller = Math.min(hkexTds.size(), count);
        Long rdate = hkexTds.get(smaller - 1).getRdate();
        return LocalDate.parse(rdate + "", DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Override
    public Long queryBeforeTradingCalendars(LocalDate localDate, Integer count) {
        long dateLong = Long.parseLong(localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                lt("rdate", dateLong).orderByDesc("rdate").last("limit " + count));
        Long rdate = hkexTds.get(count - 1).getRdate();
        return rdate;
    }

    @Override
    public List<Long> getLastSixTradingCalendars(LocalDate date, Integer count) {
        /**
         * 1、通过当前日期先获取到最近六个季度的最后一天
         * 2、分别判断这六天是不是交易日，不是交易日往前退，退到交易日为止
         */
        List<Long> dates = new ArrayList<>();

        List<BgTradingCalendar> bgTradingCalendars = new ArrayList<>();
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>()
                .lt("rdate", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .orderByDesc("rdate")
                .last("limit " + 720));

        for (TradeCalendar hkexTd : hkexTds) {
            TradeCalendar date1 = getQuarterEnd(hkexTd);
            if (ObjectUtils.isNotEmpty(date1)) {
                BgTradingCalendar bgTradingCalendar = new BgTradingCalendar();
                bgTradingCalendar.setDate(LocalDate.parse(date1.getRdate() + "", DateTimeFormatter.ofPattern("yyyyMMdd")));
                //bgTradingCalendar.setTradingDay(date1.getIstrade().equals("Y") ? 1 : 0);
                bgTradingCalendar.setTradingDay(date1.getIstrade().equals("N") ? 0 : 1);
                bgTradingCalendars.add(bgTradingCalendar);
            }
            if (bgTradingCalendars.size() >= count) break;
        }
        for (BgTradingCalendar bgTradingCalendar : bgTradingCalendars) {
            if (!bgTradingCalendar.getTradingDay().equals(1)) {
                BgTradingCalendar bgTradingCalendar1 = getBeforeTradingCalendar(bgTradingCalendar.getDate());
                dates.add(Long.parseLong(bgTradingCalendar1.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            } else {
                dates.add(Long.parseLong(bgTradingCalendar.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            }
        }
        return dates;

    }

    /**
     * @Description: 判断是否是季度最后一天
     * @Author: szb
     **/
    public TradeCalendar getQuarterEnd(TradeCalendar hkexTd) {
        String monthAndDay = hkexTd.getRdate().toString().substring(4, 8);
        if (monthAndDay.equals("0331") || monthAndDay.equals("0630") || monthAndDay.equals("0930") || monthAndDay.equals("1231")) {
            return hkexTd;
        }
        return null;
    }

    @Override
    public ResultT<List<BgTradingCalendar>> getAllTradingCalendars(LocalDate date) {
        long dateLong = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                ge("rdate", dateLong).orderByDesc("rdate"));
        return ResultT.success(hkexTds.stream().map(this::build).collect(Collectors.toList()));
    }

    @Override
    public List<BgTradingCalendar> queryTradingCalendarsBySection(LocalDate startDate, LocalDate endDate) {
        long startDateLong = Long.parseLong(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long endDateLong = Long.parseLong(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N")
                .ge("rdate", startDateLong)
                .lt("rdate", endDateLong)
                .orderByAsc("rdate"));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }

    @Override
    public BgTradingCalendar queryBeforeTradingCalendars(Integer num) {
        long dateLong = Long.parseLong(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N").
                le("rdate", dateLong).orderByDesc("rdate").last("limit " + num));
        return build(hkexTds.get(num - 1));
    }

    @Override
    public boolean isTradingTimeAM(LocalTime localTime) {
        return localTime.isAfter(am00) && localTime.isBefore(am9);
    }

    @Override
    public HkTradingSessionStatusResp getTodayTradingStatus() {
        HkTradingSessionStatusReq req  = new HkTradingSessionStatusReq();
        req.setMarket("MAIN");
        req.setTimeMode(0);
        try {
            ResultT<List<HkTradingSessionStatusResp>> tradingSessionStatus = iStockBusinessApi.getTradingSessionStatus(req);
            List<HkTradingSessionStatusResp> data = tradingSessionStatus.getData();
            HkTradingSessionStatusResp resp = data.get(0);
            log.info("获取当日交易状态:{}",JSON.toJSONString(resp));
            return resp;
        }catch (Exception e){
            log.error("获取当日交易状态失败,异常:",e);
        }
        return null;
    }

    @Override
    public TradingStateResp getTradingState() {
        TradingStateResp tradingStateResp = new TradingStateResp();
        tradingStateResp.setMorningClose(Boolean.FALSE);
        tradingStateResp.setAfternoonClose(Boolean.FALSE);

        //从redis获取上午交易状态
        String statusAM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_AM);
        if (StringUtils.isNotEmpty(statusAM) && statusAM.equals(TRADING_CLOSE_AM)) {
            tradingStateResp.setMorningClose(Boolean.TRUE);
            //获取下午交易状态
            String statusPM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_PM);
            if (StringUtils.isNotEmpty(statusPM) && statusPM.equals(TRADING_CLOSE_PM)) {
                tradingStateResp.setAfternoonClose(Boolean.TRUE);
            }
        }
        log.info("获取交易状态,tradingStateResp={}",JSON.toJSONString(tradingStateResp));
        return tradingStateResp;
    }

    @Override
    public List<Long> queryLatestTradingDays(List<String> dateList) {
        return dateList.stream().map(date -> {
            long dateLong = Long.parseLong(DateUtil.format(DateUtil.parse(date), DatePattern.PURE_DATE_FORMAT));
            TradeCalendar tradeCalendar = tradeCalendarMapper.selectOne(new QueryWrapper<TradeCalendar>().ne("istrade", "N").le("rdate", dateLong).orderByDesc("rdate").last("limit 1"));
            return tradeCalendar.getRdate();
        }).collect(Collectors.toList());
    }
    @Override
    public ResultT<Map<LocalDate, Integer>> queryTradingDayMap(LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        long startDateLong = Long.parseLong(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long endDateLong = Long.parseLong(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        List<TradeCalendar> list = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>().ne("istrade", "N")
                .ge("rdate", startDateLong)
                .le("rdate", endDateLong)
                .ne("istrade", "N")
                .orderByAsc("rdate"));
        if (CollectionUtil.isEmpty(list)) {
            return ResultT.success(new HashMap<>());
        }
        Map<LocalDate, Integer> map = buildMap(list);
        return ResultT.success(map);
    }

    private Map<LocalDate, Integer> buildMap(List<TradeCalendar> list) {
        Map<LocalDate, Integer> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            TradeCalendar tradeCalendar = list.get(i);
            Long rDate = tradeCalendar.getRdate();
            LocalDate date = LocalDate.parse(rDate + "", DateTimeFormatter.ofPattern("yyyyMMdd"));
            map.put(date, i);
        }
        return map;
    }


    @Override
    public List<BgTradingCalendar> getTradingCalendarByDate(LocalDate startDate, LocalDate endDate) {
        long startDateLong = Long.parseLong(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long endDateLong = Long.parseLong(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>()
                .ne("istrade", "N")
                .ge("rdate", startDateLong)
                .le("rdate", endDateLong)
                .orderByDesc("rdate"));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }

    @Override
    public List<BgTradingCalendar> getTradingCalendarByDateNoToday(LocalDate startDate, LocalDate endDate) {
        long startDateLong = Long.parseLong(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long endDateLong = Long.parseLong(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        List<TradeCalendar> hkexTds = tradeCalendarMapper.selectList(new QueryWrapper<TradeCalendar>()
                .ne("istrade", "N")
                .ge("rdate", startDateLong)
                .lt("rdate", endDateLong)
                .orderByDesc("rdate"));
        return hkexTds.stream().map(this::build).collect(Collectors.toList());
    }
}
