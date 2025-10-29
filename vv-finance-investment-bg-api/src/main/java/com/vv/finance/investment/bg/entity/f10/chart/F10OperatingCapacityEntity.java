package com.vv.finance.investment.bg.entity.f10.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ToString
@Data
public class F10OperatingCapacityEntity implements Serializable {

    private static final long serialVersionUID = -6843269561050446107L;

    @ApiModelProperty(value = "总资产周转率")
    private String[] totalAssetsTurnover;
    @ApiModelProperty(value = "股东权益周转率")
    private String[] tose;
    @ApiModelProperty(value = "运营周转")
    private String[] operationalTurnover;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
