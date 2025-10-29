package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/7/19 16:56
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityNonFinancial implements Serializable {
    private static final long serialVersionUID = 5324929052155803759L;
    /**
     * 毛利率
     */
    private F10Val profitRatio;

    /**
     * 经营利润率
     */
    private F10Val operatingProfitRatio;

    /**
     * 税前利润率
     */
    private F10Val earningBeforeTaxRatio;
    /**
     *净利润率
     */
    private F10Val netProfitRatio;

    /**
     *核心利润率
     */
    private F10Val coreProfitRatio;

    /**
     *股东权益回报率
     */
    private F10Val roe ;
    /**
     * 资产回报率
     */
    private F10Val roa;

    /**
     * 资本运用回报率
     */
    private F10Val roce;


    /**
     *平均股东权益回报率
     */
    private F10Val averageRoe;
    /**
     * 平均资产回报率
     */
    private F10Val averageRoa;

    /**
     * 平均资本运用回报率
     */
    private F10Val averageRoce;


    /**
     * 销售净利率
     */
    private F10Val npoms;

    /**
     * 企业价值倍数
     */
    private BigDecimal evebitda;
}
