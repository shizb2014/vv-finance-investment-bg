package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/28 15:38
 */
@Data
public class StockDetailDTO implements Serializable {
    /**
     * 股票代码
     */
    @ApiModelProperty(value = "股票代码")
    private String code;

    /**
     * 股票名
     */
    @ApiModelProperty(value ="股票名")
    private String name;

    /**
     * 最新价
     */
    @ApiModelProperty(value ="最新价")
    private BigDecimal last;

    /**
     * 昨收
     */
    @ApiModelProperty(value ="昨收")
    private BigDecimal preclose;

    /**
     * 今日最高
     */
    @ApiModelProperty(value ="今日最高")
    private BigDecimal high;

    /**
     * 今日最低
     */
    @ApiModelProperty(value ="今日最低")
    private BigDecimal low;

    /**
     * 成交额
     */
    @ApiModelProperty(value ="成交额")
    private BigDecimal turnover;

    /**
     * 成交量
     */
    @ApiModelProperty(value ="成交量")
    private BigDecimal sharestraded;

    /**
     * 总股本
     */
    @ApiModelProperty(value ="总股本")
    private BigDecimal totalEquity;

    /**
     * 市盈率(静)
     */
    @ApiModelProperty(value ="市盈率(静)")
    private BigDecimal peRatio;

    /**
     * 市盈率(ttm)
     */
    @ApiModelProperty(value ="市盈率(ttm)")
    private BigDecimal peTTM;

    /**
     * 换手率
     */
    @ApiModelProperty(value ="换手率")
    private BigDecimal turnoverRate;

    /**
     * 振幅
     */
    @ApiModelProperty(value ="振幅")
    private BigDecimal swing;

    /**
     * 市净率
     */
    @ApiModelProperty(value ="市净率")
    private BigDecimal pbRatio;

    /**
     * 52周最高
     */
    @ApiModelProperty(value ="52周最高")
    private BigDecimal fthigh;

    /**
     * 平均价
     */
    @ApiModelProperty(value ="平均价")
    private BigDecimal vwap;

    /**
     * 量比
     */
    @ApiModelProperty(value ="量比")
    private BigDecimal quantityRelativeRatio;

    /**
     * 52周最低
     */
    @ApiModelProperty(value ="52周最低")
    private BigDecimal ftlow;

    /**
     * 股息TTM
     */
    @ApiModelProperty(value ="股息TTM")
    private BigDecimal dividend;

    /**
     * 股息率TTM
     */
    @ApiModelProperty(value ="股息率TTM")
    private BigDecimal dividendRate;

    /**
     * 历史最高
     */
    @ApiModelProperty(value ="历史最高")
    private BigDecimal historyHigh;

    /**
     * 历史最低
     */
    @ApiModelProperty(value ="历史最低")
    private BigDecimal historyLow;

    /**
     * 交易状态
     */
    private Integer suspension;
}
