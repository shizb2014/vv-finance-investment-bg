package com.vv.finance.investment.bg.stock.select.dto;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 策略选股利润表指标
 *
 * @author wsliang
 * @date 2022/3/3 15:58
 **/
@Data
public class CashFlowSelectDto extends BaseFinanceSelectDto {

    @ApiModelProperty(value = "经营活动现金流")
    private BigDecimal cashFlowFromeOperations;

    @ApiModelProperty(value = "投资活动现金流")
    private BigDecimal cashFlowFromInvestmentActivities;

    @ApiModelProperty(value = "融资活动现金流")
    private BigDecimal cashFlowFromFinancingActivites;

    @Override
    public String toString() {
        return "CashFlowFromSelectDto{" +
                "cashFlowFromeOperations=" + cashFlowFromeOperations +
                ", cashFlowFromInvestmentActivities=" + cashFlowFromInvestmentActivities +
                ", cashFlowFromFinancingActivites=" + cashFlowFromFinancingActivites +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
