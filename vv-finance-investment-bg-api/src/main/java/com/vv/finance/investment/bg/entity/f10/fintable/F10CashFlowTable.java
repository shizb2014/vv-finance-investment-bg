package com.vv.finance.investment.bg.entity.f10.fintable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName F10CashFlowTable
 * @Deacription 现金流量表
 * @Author lh.sz
 * @Date 2021年07月24日 10:43
 **/
@Data
@ToString
public class F10CashFlowTable implements Serializable {

    private static final long serialVersionUID = -1883900656918954983L;

    @ApiModelProperty(value = "报告类型")
    private String reportType;

    @ApiModelProperty(value = "经营活动现金流")
    private BigDecimal cashFlowFromOperations;

    @ApiModelProperty(value = "投资活动现金流")
    private BigDecimal cashFlowFromInvestmentActivities;

    @ApiModelProperty(value = "融资活动现金流")
    private BigDecimal cashFlowFromFinancingActivities;

    @ApiModelProperty(value = "汇率影响")
    private BigDecimal exchangeRateInfluence;

    @ApiModelProperty(value = "现金净额")
    private BigDecimal netCash;

    @ApiModelProperty(value = "期初现金")
    private BigDecimal initialCash;

    @ApiModelProperty(value = "期末现金")
    private BigDecimal endCash;

    @ApiModelProperty("时间")
    private Long time;
}
