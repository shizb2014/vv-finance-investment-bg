package com.vv.finance.investment.bg.entity.f10.financial;

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
public class SolvencyFinancial implements Serializable {

    private static final long serialVersionUID = -4157218342214896843L;
    /**
     * 贷款/存款
     */
    private F10Val loansDeposits;
    /**
     *贷款/总资产
     */
    private F10Val loansTotalAssets;
    /**
     * 贷款/股东权益
     */
    private F10Val loansStockholderEquity;
    /**
     * 贷款/权益总额
     */
    private F10Val loansTotalEquity;

    /**
     * 存款/总资产
     */
    private F10Val depositsTotalAssets;

    /**
     * 存款/股东权益
     */
    private F10Val depositsStockholderEquity;



    /**
     * 存款/权益总额
     */
    private F10Val depositsTotalEquity;
    /**
     *股东权益/总资产
     */
    private F10Val stockholderEquityTotalAssets;
    /**
     *权益总额/总资产
     */
    private F10Val totalEquityTotalAssets;


}
