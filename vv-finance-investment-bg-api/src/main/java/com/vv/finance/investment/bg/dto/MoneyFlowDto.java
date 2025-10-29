package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/12/22 11:18
 */
@Data
public class MoneyFlowDto implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty(value = "主力资金净流入")
    private BigDecimal majorNetInAmount;
    @ApiModelProperty(value = "主力资金流入")
    private BigDecimal majorInAmount;
    @ApiModelProperty(value = "主力资金流出")
    private BigDecimal majorOutAmount;
    @ApiModelProperty(value = "特大单净流入")
    private BigDecimal outsizeNetIn;
    @ApiModelProperty(value = "特大单净流入占比")
    private BigDecimal outsizeNetInProportion;
    @ApiModelProperty(value = "大单净流入")
    private BigDecimal bigNetIn;
    @ApiModelProperty(value = "大单净流入占比")
    private BigDecimal bigNetInProportion;
    @ApiModelProperty(value = "中单净流入")
    private BigDecimal middleNetIn;
    @ApiModelProperty(value = "中单净流入占比")
    private BigDecimal middleNetInProportion;
    @ApiModelProperty(value = "小单净流入")
    private BigDecimal smallNetIn;
    @ApiModelProperty(value = "小单净流入占比")
    private BigDecimal smallNetInProportion;
    @ApiModelProperty(value = "行情时间")
    private Long time;

}
