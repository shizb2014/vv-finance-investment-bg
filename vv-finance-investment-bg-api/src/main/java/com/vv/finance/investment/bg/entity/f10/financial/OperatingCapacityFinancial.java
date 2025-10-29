package com.vv.finance.investment.bg.entity.f10.financial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 18:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatingCapacityFinancial implements Serializable {
    private static final long serialVersionUID = -8049429227414875336L;
    /**
     * 减值准备对客户贷款比率
     */
    private F10Val provisionForImpairmentToCustomerLoansRatio;
    /**
     * 逾期贷款比率
     */
    private F10Val overdueLoanRatio;
    /**
     * 经重组贷款比率
     */
    private F10Val restructuredLoanRatio;
    /**
     * 资本重组比率
     */
    private F10Val recapitalizationRatio;

    /**
     * 平均流动资金比率
     */
    private F10Val averageLiquidityRatio;
}
