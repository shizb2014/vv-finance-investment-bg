package com.vv.finance.investment.bg.constants;


/**
 * @author lh.sz K线 分时入参
 */

public class KlineReqConstants {
    /**
     * 分钟数，支持1，5，15，30
     */
    public final static  int MINC = 1;
    /**
     * 数量
     */
    public final static int ONE_NUMBER = 1;
    /**
     * forward：前复权；backward：后复
     * 权，为空不复权
     */
    public final static String ADJHKT = "";
    /**
     * 均线A天数
     */
    public final static int MA_A = 5;
    /**
     * 均线A天数
     */
    public final static int MA_B = 10;
    /**
     * 均线A天数
     */
    public final static int MA_C = 15;
    /**
     * 模式：dly：延时，rt：实时
     */
    public final static String MODE = "rt";
}
