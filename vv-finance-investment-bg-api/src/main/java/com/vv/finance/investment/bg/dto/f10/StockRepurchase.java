package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 19:16
 * 股票回购
 */
@Data
public class StockRepurchase implements Serializable {
    private static final long serialVersionUID = 1662767835858325690L;
    @ApiModelProperty(value = "回购日期")
    private String repurchaseDate;

    @ApiModelProperty(value = "回购金额")
    private BigDecimal repurchaseAmount;

    @ApiModelProperty(value = "回购数量（股）")
    private BigDecimal repurchaseQuantity;

    @ApiModelProperty(value = "回购最高价")
    private BigDecimal repurchaseHighestPrice;


    @ApiModelProperty(value = "回购最低价")
    private BigDecimal repurchaseLowestPrice;

    @ApiModelProperty(value = "平均回购价")
    private BigDecimal repurchaseAvgPrice;

    @ApiModelProperty(value = "年初至今累计回购数量（股）")
    private BigDecimal cumulativeRepurchaseQuantity;

    @ApiModelProperty(value = "币种")
    private String currency;


}
