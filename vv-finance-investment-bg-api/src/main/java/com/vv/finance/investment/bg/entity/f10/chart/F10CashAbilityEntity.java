package com.vv.finance.investment.bg.entity.f10.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class F10CashAbilityEntity implements Serializable {

    private static final long serialVersionUID = 858799054526186206L;

    @ApiModelProperty(value = "流动比率")
    private BigDecimal currentRatio;
    @ApiModelProperty(value = "速动比率")
    private BigDecimal quickRatio;
    @ApiModelProperty(value = "现金比率")
    private BigDecimal cashRatio;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
