package com.vv.finance.investment.bg.dto.southward.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author qinxi
 * @date 2023/6/25 11:38
 * @description: 南向资金详情列表响应
 */
@Data
public class SouthwardCapitalStockDetailResp implements Serializable {


    private static final long serialVersionUID = -7150415068714094422L;

    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty("最新价")
    private BigDecimal last;

    @ApiModelProperty("涨跌额")
    private BigDecimal chg;

    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty("昨日净流入")
    private BigDecimal netTurnoverIn;

    @ApiModelProperty("近5日净流入")
    private BigDecimal netTurnoverInNearly5Days;

    @ApiModelProperty("近20日净流入")
    private BigDecimal netTurnoverInNearly20Days;

    @ApiModelProperty("近60日净流入")
    private BigDecimal netTurnoverInNearly60Days;

    @ApiModelProperty("昨日净买卖股数")
    private BigDecimal netBuyingShares;

    @ApiModelProperty("近5日净买卖股数")
    private BigDecimal netBuyingSharesNearly5Days;

    @ApiModelProperty("近20日净买卖股数")
    private BigDecimal netBuyingSharesNearly20Days;

    @ApiModelProperty("近60日净买卖股数")
    private BigDecimal netBuyingSharesNearly60Days;

    @ApiModelProperty("总排名数量")
    private Integer totalRank;

    @ApiModelProperty("昨日资金排名")
    private Integer turnoverRank;

    @ApiModelProperty("近5日资金排名")
    private Integer turnoverRankNearly5Days;

    @ApiModelProperty("近20日资金排名")
    private Integer turnoverRankNearly20Days;

    @ApiModelProperty("近60日资金排名")
    private Integer turnoverRankNearly60Days;

    @ApiModelProperty("昨日增持比例")
    private BigDecimal holdingIncreaseRate;

    @ApiModelProperty("近5日增持比例")
    private BigDecimal holdingIncreaseRateNearly5Days;

    @ApiModelProperty("近20日增持比例")
    private BigDecimal holdingIncreaseRateNearly20Days;

    @ApiModelProperty("近60日增持比例")
    private BigDecimal holdingIncreaseRateNearly60Days;

    @ApiModelProperty("昨日持股市值")
    private BigDecimal holdingMarkValue;

    @ApiModelProperty("昨日持股比例")
    private BigDecimal holdingRate;
    private Integer stockType ;
    private Integer regionType ;



}
