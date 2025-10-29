package com.vv.finance.investment.bg.entity.f10.insurance;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.*;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 16:56
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityInsurance implements Serializable {
    private static final long serialVersionUID = 5324929052155803759L;
    /**
     * 赔款比率
     */
    private F10Val indemnityRatio;

    /**
     * 税前利润率
     */
    private F10Val earningBeforeTaxRatio;

    /**
     * 净利润率
     */
    private F10Val netProfitRatio;

    /**
     * 股东权益回报率
     */
    private F10Val roe;

    /**
     * 资产回报率
     */
    private F10Val roa;
    /**
     * 平均股东权益回报率
     */
    private F10Val averageRoe;

    /**
     * 平均资产回报率
     */
    private F10Val averageRoa;

}
