package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/27 14:06
 */
@Data
public class StockBaseDTO implements Serializable {
 

    @ApiModelProperty(value = "股票ID", hidden = true, readOnly = true)
    private Long id;

    /**
     * 股票代码
     */
    @ApiModelProperty(value = "股票代码")
    private String code;
    /**
     * 股票名称
     */
    @ApiModelProperty(value = "股票名称")
    private String name;
    /**
     * 现价
     */
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    /**
     * 涨跌价
     */
    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;
    /**
     * 最新价-昨收价 = 涨跌额 sdk没有
     */
    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;
    /**
     * 涨幅  sdk没有
     */
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;
    /**
     * 五分钟涨跌幅
     */
    @ApiModelProperty(value = "5分钟涨跌幅")
    private BigDecimal fiveMinutesChgPct;

    @ApiModelProperty(value = "60日涨跌幅")
    private BigDecimal sixtyDayChgPct;

    @ApiModelProperty(value = "年初至今涨跌")
    private BigDecimal yearToNowChgPct;

    @ApiModelProperty(value = "月初至今涨跌")
    private BigDecimal monthToNowChgPct;

    @ApiModelProperty("区域代码")
    private Integer regionType;
    @ApiModelProperty("股票类型 1-正股 2-ETF 3-权证 4-指数 5-板块 6-其他 7-基金 8-债券 9-ETN 10-otc 11-概念 12-行业")
    private Integer stockType;

    @ApiModelProperty("股票状态")
    private Integer suspension;

}
