package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CostProfitability implements Serializable {
    private static final long serialVersionUID = 4940018854256917661L;

    /**
     * 成本费用利润率
     */
    private F10Val costProfitMargin;
    /**
     * 成本费用率
     */
    private F10Val costRate;
    /**
     * 主营成本占营收比率
     */
    private F10Val majorCostRatio;
    /**
     * 销售费用占比
     */
    private F10Val salesExpenseRatio;
    /**
     * 行政费用占比
     */
    private F10Val administrativeCostsRatio;
    /**
     * 财务成本占比
     */
    private F10Val financialCostsRatio;
}
