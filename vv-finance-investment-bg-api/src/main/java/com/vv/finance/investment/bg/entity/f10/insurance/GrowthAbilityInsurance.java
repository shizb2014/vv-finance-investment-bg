package com.vv.finance.investment.bg.entity.f10.insurance;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 17:22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrowthAbilityInsurance implements Serializable {
    private static final long serialVersionUID = 7545614835450324065L;
    /**
     * 营业收入增长率
     */
    private F10Val operatingRevenueGrowth;
    /**
     * 净利润增长率
     */
    private  F10Val netProfitGrowth;

    /**
     * 总资产增长率
     */
    private F10Val totalAssetsGrowth;

    /**
     * 经营溢利增长率
     */
    private F10Val grossIncomeGrowth;


    /**
     * 经营溢利增长率
     */
    private F10Val earningPerShareGrowth;



}
