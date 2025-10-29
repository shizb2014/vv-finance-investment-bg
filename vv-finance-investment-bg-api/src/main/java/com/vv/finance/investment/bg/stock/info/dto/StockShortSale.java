package com.vv.finance.investment.bg.stock.info.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StockShortSale
 * @Deacription 股票沽空数据
 * @Author lh.sz
 * @Date 2021年11月12日 16:29
 **/
@Data
@ToString
@Builder
public class StockShortSale implements Serializable {

    private static final long serialVersionUID = -1;
    @ApiModelProperty("唯一ID")
    private Long stockId;
    @ApiModelProperty("股票代码")
    private String stockCode;
    @ApiModelProperty("沽空比率")
    private BigDecimal shortSaleRate;
    @ApiModelProperty("大市沽空比率")
    private BigDecimal marketShortSaleRate;
    @ApiModelProperty("沽空均价")
    private BigDecimal shortSaleAvg;
    @ApiModelProperty("收盘价")
    private BigDecimal close;
    @ApiModelProperty("沽空量")
    private BigDecimal shortSaleNum;
    @ApiModelProperty("更新时间")
    private Long time;
    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;
}
