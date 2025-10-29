package com.vv.finance.investment.bg.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName: DateUtils
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/2   18:37
 */
public class TimeConvertUtil {

    private TimeConvertUtil(){}

    public static void main(String[] args) {
        // System.err.println(getEndDayOfQuarter());
        // System.err.println(getEndDayOfQuarter(20230512L));
        // System.err.println(getEndDayOfQuarter(DateUtil.parse("20230825")));
        System.err.println(getEndDayOfQuarter());
        System.err.println(getEndOfLastQuarter());
        System.err.println(getBeginDayOfQuarterForDate(DateUtil.date(), 8));
        System.err.println(getBeginDayOfQuarter(DateUtil.date()));
        System.err.println(getEndDayOfQuartersForDate(3));
        System.err.println(getEndTimestampsForQuarter(3));
        System.err.println(getEndDayOfQuartersForDate(20240331L, 3));
    }

    /**
     * 当前季度最后一天 yyyyMMdd
     * @return {@link Long}
     */
    public static Long getEndDayOfQuarter() {
        return getEndDayOfQuarter(DateUtil.date());
    }

    /**
     * 指定季度最后一天 yyyyMMdd
     * @param ymd yyyyMMdd
     * @return {@link Long}
     */
    public static Long getEndDayOfQuarter(Long ymd) {
        return getEndDayOfQuarter(DateUtil.parse(String.valueOf(ymd), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 指定季度最后一天 yyyyMMdd
     * @param ymd yyyyMMdd
     * @return {@link Long}
     */
    public static DateTime getEndDateOfQuarter(Long ymd) {
        return DateUtil.endOfQuarter(DateUtil.parse(String.valueOf(ymd), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 上个季度第一天
     * @return {@link Long}
     */
    public static DateTime getEndOfLastQuarter() {
        // 上个季度 = 当前季度-3个月
        DateTime dateTime = DateUtil.endOfQuarter(DateUtil.date());
        DateTime lastQua = DateUtil.offsetMonth(dateTime, -3);
        return DateUtil.endOfQuarter(lastQua);
    }

    /**
     * 指定日期前几个季度第一天 (yyyyMMdd)
     * @param quaSize
     * @return {@link Long}
     */
    public static List<Long> getEndDayOfQuartersForDate(Integer quaSize) {
        // n个季度前 = 当前季度-3个月 * n
        DateTime dateTime = getEndOfLastQuarter();
        List<Long> dayList = IntStream.range(0, quaSize).mapToObj(i -> {
            DateTime date = DateUtil.offsetMonth(dateTime, -3 * i);
            return getEndDayOfQuarter(date);
        }).collect(Collectors.toList());
        return dayList;
    }

    /**
     * 指定日期前几个季度第一天 (yyyyMMdd)
     * @param quaSize
     * @return {@link Long}
     */
    public static List<Long> getEndDayOfQuartersForDate(Long ymd, Integer quaSize) {
        // n个季度前 = 当前季度-3个月 * n
        DateTime dateTime = getEndDateOfQuarter(ymd);
        List<Long> dayList = IntStream.range(0, quaSize).mapToObj(i -> {
            DateTime date = DateUtil.offsetMonth(dateTime, -3 * i);
            return getEndDayOfQuarter(date);
        }).collect(Collectors.toList());
        return dayList;
    }

    /**
     * 指定日期前几个季度第一天(timestamp)
     * @param quaSize
     * @return {@link Long}
     */
    public static List<Long> getEndTimestampsForQuarter(Integer quaSize) {
        // n个季度前 = 当前季度-3个月 * n
        DateTime dateTime = getEndOfLastQuarter();
        List<Long> timeList = IntStream.range(0, quaSize).mapToObj(i -> {
            DateTime date = DateUtil.offsetMonth(dateTime, -3 * i);
            return getEndTimestampOfQuarter(date);
        }).collect(Collectors.toList());
        return timeList;
    }

    /**
     * 指定季度最后一天 yyyyMMdd
     * @param date
     * @return {@link Long}
     */
    public static Long getEndDayOfQuarter(Date date) {
        return Long.parseLong(DateUtil.format(DateUtil.endOfQuarter(date), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 指定季度最后一天 yyyyMMdd
     * @param date
     * @return {@link Long}
     */
    public static Long getEndTimestampOfQuarter(Date date) {
        LocalDate localDate = DateUtil.endOfQuarter(date).toLocalDateTime().toLocalDate();
        return LocalDateTimeUtil.toEpochMilli(localDate);
    }

    /**
     * 当前季度第一天 yyyyMMdd
     * @return {@link Long}
     */
    public static Long getBeginDayOfQuarter() {
        return getBeginDayOfQuarter(DateUtil.date());
    }

    /**
     * 上个季度第一天
     * @return {@link Long}
     */
    public static Long getBeginOfLastQuarter() {
       // 上个季度 = 当前季度-3个月
        DateTime dateTime = DateUtil.beginOfQuarter(DateUtil.date());
        DateTime lastQua = DateUtil.offsetMonth(dateTime, -3);
        return getBeginDayOfQuarter(lastQua);
    }

    /**
     * 指定日期前几个季度第一天
     * @param quaSize
     * @return {@link Long}
     */
    public static Long getBeginDayOfQuarterForDate(Integer quaSize) {
        // n个季度前 = 当前季度-3个月 * n
        return getBeginDayOfQuarterForDate(DateUtil.date(), quaSize);
    }

    /**
     * 指定日期前几个季度第一天
     * @param date
     * @param quaSize
     * @return {@link Long}
     */
    public static Long getBeginDayOfQuarterForDate(Date date, Integer quaSize) {
       // n个季度前 = 当前季度-3个月 * n
        DateTime dateTime = DateUtil.beginOfQuarter(date);
        DateTime lastQua = DateUtil.offsetMonth(dateTime, -3 * quaSize);
        return getBeginDayOfQuarter(lastQua);
    }

    /**
     * 当前季度第一天 yyyyMMdd
     * @param date
     * @return {@link Long}
     */
    public static Long getBeginDayOfQuarter(Date date) {
        return Long.parseLong(DateUtil.format(DateUtil.beginOfQuarter(date), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 获取时间戳
     * @param ymd yyyyMMdd
     * @return {@link Long}
     */
    public static Long getTimeStampByYmd(Long ymd) {
        return DateUtil.parse(String.valueOf(ymd), DatePattern.PURE_DATE_FORMAT).getTime();
    }

    /**
     * 时间戳转 yyyyMMdd
     * @param timestamp
     * @return {@link Long}
     */
    public static Long getYmdByTimeStamp(Long timestamp) {
        return Long.parseLong(DateUtil.format(DateUtil.date(timestamp), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 时间戳转 yyyyMMdd
     * @return {@link Long}
     */
    public static Long getYmdByDate() {
        return Long.parseLong(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_FORMAT));
    }

    /**
     * 时间戳转 yyyyMMdd
     * @param timestamp
     * @return {@link Long}
     */
    public static Long getYmByTimeStamp(Long timestamp) {
        return Long.parseLong(DateUtil.format(DateUtil.date(timestamp), DatePattern.SIMPLE_MONTH_PATTERN));
    }

    /**
     * 时间戳转 yyyy/MM/dd
     * @param ymd
     * @return {@link Long}
     */
    public static String getYmdStrByDay(Long ymd) {
        return DateUtil.format(DateUtil.parse(String.valueOf(ymd), DatePattern.PURE_DATE_FORMAT), "yyyy/MM/dd");
    }
}
