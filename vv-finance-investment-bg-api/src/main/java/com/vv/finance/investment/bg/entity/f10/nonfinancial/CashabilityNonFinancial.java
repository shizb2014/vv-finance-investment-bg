package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 19:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashabilityNonFinancial implements Serializable {
    private static final long serialVersionUID = 5418365399980306039L;
    /**
     * 流动比率
     */
    private F10Val currentRatio;
    /**
     * 速动比率
     */
    private  F10Val quickRatio;

    /**
     * 现金比率
     */
    private F10Val cashRatio;

    /**
     * 营业现金流比率
     */
    private F10Val operatingCashFlowRatio;

}
