package com.vv.finance.investment.bg.entity.f10.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class F10CashFlowIndicatorEntity implements Serializable {

    private static final long serialVersionUID = 7130025312410521127L;

    @ApiModelProperty(value = "销售现金比率")
    private String[] salesCashRatio;
    @ApiModelProperty(value = "现金净流量盈利")
    private String[] cashNetFlowProfit;
    @ApiModelProperty(value = "现金含量")
    private String[] cashContent;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
