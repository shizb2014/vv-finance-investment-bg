package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/12/9 15:50
 */
@Data
public class ValuationGrowth implements Serializable {
    private static final long serialVersionUID = -2513197553213622948L;
    @ApiModelProperty(value = "代码")
    private String code;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;
    @ApiModelProperty(value = "PEG")
    private BigDecimal peg;
    @ApiModelProperty(value = "市盈率TTM")
    private BigDecimal ttm;
    @ApiModelProperty(value = "每股盈利复合增长率（三年")
    private BigDecimal earningsPerShareCAGR3;
    @ApiModelProperty(value = "每股盈利复合增长率（五年)")
    private BigDecimal earningsPerShareCAGR5;
    @ApiModelProperty(value = "总资产复合增长率（三年）")
    private BigDecimal totalAssetsCAGR3;
    @ApiModelProperty(value = "总资产复合增长率（五年)")
    private BigDecimal totalAssetsCAGR5;
    @ApiModelProperty(value = "营业额复合增长率（三年）")
    private BigDecimal  turnoverCAGR3;
    @ApiModelProperty(value = "营业额复合增长率（五年）")
    private BigDecimal  turnoverCAGR5;

    @ApiModelProperty(value = "毛利润复合增长率（三年）")
    private BigDecimal grossProfitCAGR3;
    @ApiModelProperty(value = "毛利润复合增长率(五年）")
    private BigDecimal grossProfitCAGR5;
    @ApiModelProperty(value = "净利润复合增长率（三年）")
    private BigDecimal netProfitCAGR3;
    @ApiModelProperty(value = "净利润复合增长率（五年)")
    private BigDecimal netProfitCAGR5;
    @ApiModelProperty(value = "经营溢利复合增长率（三年）")
    private BigDecimal  operatingProfitCAGR3;
    @ApiModelProperty(value = "经营溢利复合增长率（五年）")
    private BigDecimal  operatingProfitCAGR5;







}
