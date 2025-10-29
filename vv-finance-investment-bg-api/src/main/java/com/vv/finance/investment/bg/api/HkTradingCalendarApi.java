package com.vv.finance.investment.bg.api;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.TradingStateResp;
import com.vv.finance.investment.bg.entity.uts.HkexTd;
import com.vv.finance.investment.gateway.dto.resp.HkTradingSessionStatusResp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hamilton
 * @date 2020/11/3 15:33
 */
public interface HkTradingCalendarApi {

    /**
     * 交易日判断yyyyMMdd
     *
     * @param date 日期
     * @return true 是
     */
    Boolean isTradingDay(LocalDate date);

    /**
     * 当前时间是否在交易时间内
     *
     * @param localDateTime
     * @return
     */
    Boolean isTradingTime(LocalDateTime localDateTime);

    /**
     * 获取交易日
     *
     * @param date
     * @return
     */
    BgTradingCalendar getTradingCalendar(LocalDate date);

    /**
     * 批量获取交易日历
     *
     * @param dateList 日期列表
     * @return {@link List}<{@link BgTradingCalendar}>
     */
    List<BgTradingCalendar> getTradingCalendarList(List<String> dateList);

    List<HkexTd> getIncrementTradingCalendar(
            Long beginTime,
            Long endTime
    );

    /**
     * 获取当前日期的下一个交易日
     *
     * @param date
     * @return
     */
    BgTradingCalendar getNextTradingCalendar(LocalDate date);

    /**
     * 获取当前日期的上一个交易日
     *
     * @param date
     * @return
     */
    BgTradingCalendar getBeforeTradingCalendar(LocalDate date);

    /**
     * 获取最近的交易日历 小于等于当前日期
     *
     * @param count 多少天
     * @return
     */
    List<BgTradingCalendar> queryLastTradingCalendars(Integer count);

    /**
     * 获取最近交易日历 大于等于当前日期
     * @param count 多少天
     * @return
     */
    List<BgTradingCalendar> queryGreaterTradingCalendars(Integer count);

    /**
     * 根据日期获取最近的交易日历 小于等于当前日期
     *
     * @param date
     * @param count
     * @return
     */
    ResultT<List<BgTradingCalendar>> getLastTradingCalendars(LocalDate date,
                                                             Integer count);

    /**
     * 根据日期获取最近的交易日历 小于当前日期
     *
     * @param date
     * @param count
     * @return
     */
    ResultT<List<BgTradingCalendar>> getLastTradingCalendarsLtDate(LocalDate date,
                                                             Integer count);

    /**
     * 获取某个日期后多少天日历 ，大于等于
     *
     * @param date
     * @param count
     * @return
     */
    List<BgTradingCalendar> queryFutureTradingCalendars(LocalDate date, Integer count);

    /**
     * 获取最近六个季度末的交易日，非交易日获取最近一个交易日
     * @param date
     * @param count
     * @return
     */
    List<Long> getLastSixTradingCalendars(LocalDate date, Integer count);


    Integer getTradingDays(LocalDate startDate, LocalDate endDate);

    /**
     * 获取开始时间和结束时间段的交易天数
     * @param startDate
     * @param endDate
     * @return
     */
    ResultT<Integer> getBetweenTradingDays(LocalDate startDate, LocalDate endDate);

    /**
     * 获取持仓天数
     *
     * @param startSureDate 建仓时间
     * @param queryDate     查询时间
     * @return 持仓天数
     */

    Integer queryTradingCountsBetweenDates(LocalDate startSureDate, LocalDate queryDate);

    /**
     * 距下一个交易日的时间
     *
     * @return 秒
     */
    Long nextTradDateSecond();

    /**
     * 当前时间是否在交易时间内
     *
     * @param localDateTime
     * @return
     */
    Boolean isTradingTimeUntilFour(LocalDateTime localDateTime);

    /**
     * 获取最近的交易日历 小于等于当前日期（具体哪一天）
     *
     * @param count 多少天
     * @return
     */
    HkexTd queryBeginTradingCalendars(Integer count);

    /**
     * 获得输入日期后多少个交易日 大于当前日期(具体哪一天)
     *
     * @param localDate 输入日期
     * @param count 多少天
     * @return
     */
    LocalDate queryAfterTradingCalendars(LocalDate localDate,Integer count);

    /**
     * 获得输入日期前多少个交易日 小于当前日期(具体哪一天)
     *
     * @param localDate 输入日期
     * @param count 多少天
     * @return
     */
    Long queryBeforeTradingCalendars(LocalDate localDate,Integer count);

    /**
     * 获取指定日期之后所有交易日
     * @param date
     * @return
     */
    ResultT<List<BgTradingCalendar>> getAllTradingCalendars(LocalDate date);

    /**
     * 获取区间内两个时间的交易日期(包含起始日期，不包含结束日期)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<BgTradingCalendar> queryTradingCalendarsBySection(LocalDate startDate, LocalDate endDate);

    /**
     * 获取小于当前日期的交易日 具体某一天
     *
     * @param num 多少天
     * @return
     */
    BgTradingCalendar queryBeforeTradingCalendars(Integer num);

    /**
     * 判断是否在上午开盘前
     *
     * @param
     * @return
     */
    boolean isTradingTimeAM(LocalTime localTime);

    /**
     * 获取当前交易状态
     * @return
     */
    HkTradingSessionStatusResp getTodayTradingStatus();


    /**
     * 获取交易状态
     * @return
     */
    TradingStateResp getTradingState();

    /**
     * 获取最近的交易日
     * @param dateList
     * @return {@link List}<{@link Long}>
     */
    List<Long> queryLatestTradingDays(List<String> dateList);

    /**
     * 获取交易日map信息
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return key:交易日 value:返回的交易日列表正序下标
     */
    ResultT<Map<LocalDate, Integer>> queryTradingDayMap(LocalDate startDate, LocalDate endDate);
    /**
     * 获取一段时间内的交易日历（不包括非交易日）
     */
    List<BgTradingCalendar> getTradingCalendarByDate(LocalDate startDate, LocalDate endDate);

    /**
     * 获取一段时间内的交易日历（不包括非交易日，不包含当日）
     */
    List<BgTradingCalendar> getTradingCalendarByDateNoToday(LocalDate startDate, LocalDate endDate);

}
