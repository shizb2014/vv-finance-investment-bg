package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 19:05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolvencyNonFinancial implements Serializable {

    private static final long serialVersionUID = -4157218342214896843L;
    /**
     * 总负债/总资产
     */
    private F10Val totalLiabilityAssets;
    /**
     *总负债/资本运用
     */
    private F10Val totalLiabilityCapitalEmployed;
    /**
     * 总负债/股东权益
     */
    private F10Val totalLiabilityStockholderEquity;
    /**
     * 总负债/权益总额
     */
    private F10Val totalLiabilityEquity;

    /**
     * 长期债务/总资产
     */
    private F10Val ltLiabilityTotalAssets;

    /**
     * 长期债务/股东权益
     */
    private F10Val ltLiabilityStockholderEquity;



    /**
     * 长期债务/权益总额
     */
    private F10Val ltLiabilityEquityTotal;
    /**
     *负债比率
     */
    private F10Val assetLiabilityRatio;
    /**
     *净负债/总资产
     */
    private F10Val netLiabilityTotalAssets;
    /**
     * 净负债/股东权益
     */
    private F10Val netLiabilityStockholderEquity;

    /**
     * 净负债/权益总额
     */
    private F10Val netLiabilityTotalEquity;

    /**
     * 股东权益/总资产
     */
    private F10Val stockholderEquityTotalAssets;

    /**
     * TODO
     * 股东权益/负债合计
     */
    private F10Val shareholdersEquityAndForms;

    /**
     * TODO
     * 有形净值债务率 (%)
     */
    private F10Val tangibleNetWorthDebtRatio;

    /**
     * TODO
     * 有形净值/净债务
     */
    private F10Val tangibleNetWorthAndNetDebt;

    /**
     * TODO
     * 息税折旧摊销前利润/总负债
     */
    private F10Val eBITDAAndTotalLiabilities;

    /**
     * TODO
     * 息税折旧摊销前利润/总负债
     */
    private F10Val eBITDAAndInterestExpense;


    /**
     * 产权比率
     */
    private F10Val equityRatio;

    /**
     * 权益乘数
     */
    private F10Val equityMultiplier;

    /**
     * 偿债保障比率
     */
    private F10Val debtCoverageRatio;

    /**
     * 利息保障倍数
     */
    private F10Val interestCoverageRatio;

    /**
     * 经营现金流量比率（长期）
     */
    private F10Val operatingCashFlowRatio;

    /**
     * 净现金流量负债比率
     */
    private F10Val netCashFlowGearingRatio;

}
