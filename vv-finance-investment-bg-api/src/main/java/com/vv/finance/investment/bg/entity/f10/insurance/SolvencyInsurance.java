package com.vv.finance.investment.bg.entity.f10.insurance;

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
public class SolvencyInsurance implements Serializable {

    private static final long serialVersionUID = -4157218342214896843L;
    /**
     * 总投资/总资产
     */
    private F10Val totalInvestmentAssets;
    /**
     *现金/总资产
     */
    private F10Val cashTotalAssets;
    /**
     * 股东权益/总资产
     */
    private F10Val stockholderEquityTotalAssets;
    /**
     * 权益总额/总资产
     */
    private F10Val totalEquityTotalAssets;

    /**
     * 债权证券/总投资
     */
    private F10Val debtSecurityTotalInvestment;

    /**
     * 股权证券/总投资
     */
    private F10Val equitySecurityTotalInvestment;

}
