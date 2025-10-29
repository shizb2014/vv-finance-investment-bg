package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName IndexConstituent
 * @Deacription 指数成分股
 * @Author lh.sz
 * @Date 2021年12月06日 11:58
 **/
@Data
@ToString
public class IndexConstituent implements Serializable {

    private static final long serialVersionUID = -6717905404821847015L;

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
    @ApiModelProperty(value = "成交量")
    private BigDecimal sharesTraded;
    @ApiModelProperty(value = "成交额")
    private BigDecimal turnover;
    @ApiModelProperty(value = "五分钟涨跌幅")
    private BigDecimal fiveMinutesChgPct;
    @ApiModelProperty(value = "年初至今涨跌幅")
    private BigDecimal yearToNowChgPct;
    @ApiModelProperty(value = "换手率")
    private BigDecimal turnoverRate;
    @ApiModelProperty(value = "量比")
    private BigDecimal quantityRelativeRatio;
    @ApiModelProperty(value = "委比")
    private BigDecimal appointThan;
    @ApiModelProperty(value = "振幅")
    private BigDecimal swing;
    @ApiModelProperty(value = "沽空量")
    private BigDecimal shortShares;
    @ApiModelProperty(value = "沽空额")
    private BigDecimal shortTurnover;
}
