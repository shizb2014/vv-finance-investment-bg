package com.vv.finance.investment.bg.stock.select.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 策略选股利润表指标
 *
 * @author wsliang
 * @date 2022/3/3 15:58
 **/
@Data
public class ProfitSelectDto extends BaseFinanceSelectDto {

    private static final long serialVersionUID = 4178207494248267032L;

    @ApiModelProperty("营业总收入")
    private BigDecimal operatingRevenue;

    @ApiModelProperty("营业总成本")
    private BigDecimal operatingCost;
    @ApiModelProperty("营业利润")
    private BigDecimal operatingProfit;
    @ApiModelProperty("归母净利润")
    private BigDecimal netProfit;
    @ApiModelProperty("税项")
    private BigDecimal tax;
    @ApiModelProperty("财务费用")
    private BigDecimal financingCost;

    @ApiModelProperty("期内损益作为净利润")
    private BigDecimal profitLossDuringPeriod;

    @Override
    public String toString() {
        return "ProfitSelectDto{" +
                "operatingRevenue=" + operatingRevenue +
                ", operatingCost=" + operatingCost +
                ", operatingProfit=" + operatingProfit +
                ", netProfit=" + netProfit +
                ", tax=" + tax +
                ", financingCost=" + financingCost +
                '}' + "super:" + super.toString();
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
