package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/12/22 11:13
 */
@Data
public class DdePolicyDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "行情时间")
    private Long time;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "股票名称")
    private String name;
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "DDE决策DDX")
    private BigDecimal ddx;
    @ApiModelProperty(value = "DDE决策DDY")
    private BigDecimal ddy;
    @ApiModelProperty(value = "DDE决策DDZ")
    private BigDecimal ddz;
    @ApiModelProperty(value = "DDE决策DDX连续飘红天数")
    private BigDecimal ddxContinuousWaveRedDays;
    @ApiModelProperty(value = "DDE决策五日DDX")
    private BigDecimal fiveDayDdx;
    @ApiModelProperty(value = "DDE决策五日DDY")
    private BigDecimal fiveDayDdy;
    @ApiModelProperty(value = "DDE决策十日DDX")
    private BigDecimal tenDayDdx;
    @ApiModelProperty(value = "DDE决策十日DDY")
    private BigDecimal tenDayDdy;
    @ApiModelProperty(value = "DDE决策5日内飘红天数")
    private BigDecimal fiveDayWaveRedDays;
    @ApiModelProperty(value = "DDE决策10日内飘红天数")
    private BigDecimal tenDayWaveRedDays;

}
