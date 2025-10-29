package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author:maling
 * @Date:2023/6/26
 * @Description:AH股详情
 */
@Data
public class AhStockDetailResp implements Serializable {
    private static final long serialVersionUID = -6481640429289499476L;

    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty("最新价")
    private BigDecimal last;

    @ApiModelProperty("A股最新价(人民币)")
    private BigDecimal cnPrice;

    @ApiModelProperty("A股最新价(港元)")
    private BigDecimal cnEquivPrice;

    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty("A股涨跌幅(人民币)")
    private BigDecimal cnChangeRate;

    @ApiModelProperty("A股换算后涨跌幅(港元)")
    private BigDecimal cnRelChangeRate;

    @ApiModelProperty("溢价率")
    private BigDecimal ahPremiumRate;

    @ApiModelProperty("涨跌额")
    private BigDecimal chg;

    @ApiModelProperty("A股涨跌额(人民币)")
    private BigDecimal cnChange;

    @ApiModelProperty("A股换算后涨跌额(港元)")
    private BigDecimal cnRelChange;

    @ApiModelProperty("A股代码")
    private String cnSymbol;

    @ApiModelProperty("股息率TTM")
    private BigDecimal dividendRateTtm;

    @ApiModelProperty("市盈率TTM")
    private BigDecimal peTtm;

    @ApiModelProperty("总市值")
    private BigDecimal totalValue;

    private Integer stockType ;
    private Integer regionType ;
}