package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice implements Serializable {
    private static final long serialVersionUID = -5539608414935308182L;
    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "日期")
    private Long date;

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal rangeability;

    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;

    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;

    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;
}
