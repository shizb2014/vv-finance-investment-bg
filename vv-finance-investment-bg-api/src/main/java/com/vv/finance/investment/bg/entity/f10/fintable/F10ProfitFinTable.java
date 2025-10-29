package com.vv.finance.investment.bg.entity.f10.fintable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName F10ProfitTable
 * @Deacription 利润表(金融)
 * @Author lh.sz
 * @Date 2021年07月24日 10:41
 **/
@Data
@ToString
public class F10ProfitFinTable implements Serializable {

    private static final long serialVersionUID = -1883900656918954983L;

    @ApiModelProperty(value = "报告类型")
    private String reportType;

    @ApiModelProperty(value = "营业总收入")
    private BigDecimal grossRevenue;

    @ApiModelProperty(value = "营业总支出")
    private BigDecimal totalOperatingExpenses;

    @ApiModelProperty(value = "经营溢利")
    private BigDecimal operatingProfit;

    @ApiModelProperty(value = "除税前溢利")
    private BigDecimal profitBeforeTaxation;

    @ApiModelProperty(value = "期内损益")
    private BigDecimal profitAndLossDuringThePeriod;

    @ApiModelProperty(value = "每股摊薄盈利")
    private BigDecimal dilutedEarningsPerShare;

    @ApiModelProperty(value = "每股基本盈利")
    private BigDecimal basicEarningsPerShare;

    @ApiModelProperty("时间")
    private Long time;
}
