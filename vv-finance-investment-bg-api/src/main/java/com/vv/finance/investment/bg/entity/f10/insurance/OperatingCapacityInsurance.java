package com.vv.finance.investment.bg.entity.f10.insurance;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.*;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 18:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatingCapacityInsurance implements Serializable {
    private static final long serialVersionUID = -8049429227414875336L;
    /**
     * 毛承保费及保单费增长
     */
    private F10Val grossPremiumsPolicyFeeGrowth;
    /**
     * 净承保费及保单费增长
     */
    private F10Val netPremiumsPolicyFeeGrowth;
    /**
     * 已赚取保费及保单费增长
     */
    private F10Val earnedPremiumsPolicyFeeGrowth;
    /**
     * 净投资收益增长
     */
    private F10Val netInvestmentIncomeGrowth;

    /**
     * 保险准备金增长
     */
    private F10Val insuranceReserveGrowth;
}
