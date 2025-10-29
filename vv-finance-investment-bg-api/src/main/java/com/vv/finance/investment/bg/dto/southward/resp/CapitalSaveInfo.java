package com.vv.finance.investment.bg.dto.southward.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author:maling
 * @Date:2023/6/26
 * @Description:
 */
@Data
public class CapitalSaveInfo implements Serializable {

    private static final long serialVersionUID = 8185222823083438368L;
    /**
     * 日期
     */
    private Long time;

    /**
     * 港股通(沪)净买入金额
     */
    private BigDecimal shNetBuyingTurnover;

    /**
     * 港股通(深)净买入金额
     */
    private BigDecimal szNetBuyingTurnover;

    /**
     * 南向资金净买入金额
     */
    private BigDecimal allNetBuyingTurnover;

    /**
     * 港股通(沪)净流入金额
     */
    private BigDecimal shNetTurnoverIn;

    /**
     * 港股通(深)净流入金额
     */
    private BigDecimal szNetTurnoverIn;

    /**
     * 南向资金净流入金额
     */
    private BigDecimal allNetTurnoverIn;

    /**
     * 港股通(沪)资金余额
     *
     */
    private BigDecimal shSurplusQuota;

    /**
     * 港股通(深)资金余额
     */
    private BigDecimal szSurplusQuota;

    /**
     * 南向资金资金余额
     */
    private BigDecimal allSurplusQuota;

    /**
     * 港股通(沪)每日额度/初始额度
     */
    private BigDecimal shDailyQuota;

    /**
     * 港股通(深)每日额度/初始额度
     */
    private BigDecimal szDailyQuota;

    /**
     * 南向资金每日额度/初始额度
     */
    private BigDecimal allDailyQuota;

    /**
     * 恒生指数
     */
    private BigDecimal hengShengIndex;
}