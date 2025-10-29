package com.vv.finance.investment.bg.constants;



/**
 * @author hamilton
 * @date 2021/6/22 11:45
 */
public interface TradingCalendarState {
    /**
     *  "0 不是交易日 1交易日-全天交易 2 上午交易 3 下午交易"
     */
    int NO_TRADING=0;
    /**
     *  "0 不是交易日 1交易日-全天交易 2 上午交易 3 下午交易"
     */
    int TRADING=1;
    /**
     *  "0 不是交易日 1交易日-全天交易 2 上午交易 3 下午交易"
     */
    int AM_TRADING=2;
    int PM_TRADING=3;
}
