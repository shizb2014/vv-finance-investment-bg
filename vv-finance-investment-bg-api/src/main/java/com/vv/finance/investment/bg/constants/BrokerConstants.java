package com.vv.finance.investment.bg.constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: shizhibiao
 * @Date: 2022/10/17
 * @Description: com.vv.finance.investment.bg.constants
 * @version: 1.0
 */
public class BrokerConstants {


    /**
     * 经纪商实时数据更新时间
     */
    public static final String BROKER_UPDATETIME = "BG:BROKER:UPDATETIME:";

    /**
     * 获取行业名称
     */
    public static final String BROKER_CODE_INDUSTRY_PROFIT = "BG:BROKER:CODEINDUSTRY:";

    /**
     * 经纪商名称
     */
    public static final String BG_BROKER_ID_PROFIT = "BG:BROKER:ID:";

    /**
     * 经纪商名称(List)
     */
    public static final String BG_BROKER_ID_PROFIT_LIST = "BG:BROKER:ID:LIST:";

    /**
     * 四位经纪商id 与6位经纪商id之间的关系
     */
    public static final String BG_BROKER_ID_RELATION_PROFIT = "BG:BROKER:IDRELATION:";

    /**
     * 经纪商持仓统计
     */
    public static final String POSITION_STATISTICS_DETAIL = "BG:BROKER:PositionStatisticsDetail:";

    /**
     * 经纪商前日集中度
     */
    public static final String BROKER_TOP5CHANGE = "BG:BROKER:Top5Change:";

    /**
     * 经纪商前日集中度
     */
    public static final String BROKER_TOP10CHANGE = "BG:BROKER:Top10Change:";

    /**
     * 30天内前五的持股经纪商
     */
    public static final String BROKER_TOP5 = "BG:BROKER:Top5:";

    /**
     * 近五天变动量
     */
    public static final String BROKER_5DAY = "BG:BROKER:5day:";

    /**
     * 近10天变动量
     */
    public static final String BROKER_10DAY = "BG:BROKER:10day:";

    /**
     * 近20天变动量
     */
    public static final String BROKER_20DAY = "BG:BROKER:20day:";

    /**
     * 近60天变动量
     */
    public static final String BROKER_60DAY = "BG:BROKER:60day:";

    /**
     * 近一年变动量
     */
    public static final String BROKER_360DAY = "BG:BROKER:360day:";

    /**
     * 经纪商维度当天变动量
     */
    public static final String BROKER_TODAY = "BG:BROKER:today:";

    /**
     * Top10集中度变动
     */
    public static final String BROKER_TOP10CHANGETODAY = "BG:BROKER:Top10ChangeToday:";

    /**
     * Top10集中度
     */
    public static final String BROKER_TOP10CONCENTRATION = "BG:BROKER:Top10Concentration:";

    /**
     * Top5集中度变动
     */
    public static final String BROKER_TOP5CHANGETODAY = "BG:BROKER:Top5ChangeToday:";

    /**
     * Top5集中度
     */
    public static final String BROKER_TOP5CONCENTRATION = "BG:BROKER:Top5Concentration:";

    /**
     * 持股比例变动
     */
    public static final String BROKER_TODAYQUANTITY = "BG:BROKER:todayQuantity:";

    /**
     * 持股市值变动
     */
    public static final String BROKER_TODAYMARKETVAL = "BG:BROKER:todayMarketVal:";

    /**
     * 今日持股比例
     */
    public static final String BROKER_TODAYPROPORTIONSHAREHOLD = "BG:BROKER:todayProportionSharehold:";

    /**
     * 经纪商维度持股变动量
     */
    public static final String BROKER_HOLD_NUM_VALUE = "BG:BROKER:todayBrokerHoldNum:";

    /**
     * 经纪商维度变动市值
     */
    public static final String BROKER_MARKET_VALUE = "BG:BROKER:todayBrokerMarketValue:";

    /**
     * 经纪商股票维度持有量
     */
    public static final String BROKER_CODE_HOLD_NUM_VALUE = "BG:BROKER:todayBrokerCodeHoldNum:";

    /**
     * 经纪商股票维度市值
     */
    public static final String BROKER_CODE_MARKET_VALUE = "BG:BROKER:todayBrokerCodeMarketValue:";

    /**
     * 经纪商维度数据(变动持股量和变动市值)
     */
    public static final String BROKER_VALUE = "BG:BROKER:todayBrokerValue:";

    /**
     * 经纪商维度当天数据(List)
     */
    public static final String BROKER_VALUE_LIST = "BG:BROKER:todayValueList:";

    /**
     * 行业-经纪商持股市值走势图MAP
     */
    public static final String BROKER_INDUSTRY_VIEW = "BG:BROKER:INDUSTRY:VIEW";

    /**
     * 深港通经纪商ID
     */
    public static final String SZ_BROKER_ID = "A00004";

    /**
     * 沪港通经纪商ID
     */
    public static final String SH_BROKER_ID = "A00003";

    public static Map<String, String> BG_BROKER_ID_PROFIT_MAP = new ConcurrentHashMap<>();

    public static Map<String, String> BG_BROKER_ID_RELATION_PROFIT_MAP = new ConcurrentHashMap<>();

}
