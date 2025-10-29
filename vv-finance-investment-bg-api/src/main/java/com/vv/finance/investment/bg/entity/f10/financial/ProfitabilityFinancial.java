package com.vv.finance.investment.bg.entity.f10.financial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 16:56
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityFinancial implements Serializable {
    private static final long serialVersionUID = 5324929052155803759L;
    /**
     * 净利率
     */
    private F10Val netProfitRatio;
    /**
     * 成本/收入
     */
    private F10Val costRevenue;

    /**
     * 净利息收益率
     */
    private F10Val netInterestMargin;

    /**
     * 贷款回报率
     */
    private F10Val loanReturn;
    /**
     * 存款回报率
     */
    private F10Val returnOnDeposit;

    /**
     * 股东权益回报率
     */
    private F10Val roe;

    /**
     * 资产回报率
     */
    private F10Val roa;
    /**
     * 平均贷款回报率
     */
    private F10Val averageLoanReturn;

    /**
     * 平均存款回报率
     */
    private F10Val averageReturnOnDeposit;


    /**
     * 平均股东权益回报率
     */
    private F10Val averageRoe;
    /**
     * 平均资产回报率
     */
    private F10Val averageRoa;


}
