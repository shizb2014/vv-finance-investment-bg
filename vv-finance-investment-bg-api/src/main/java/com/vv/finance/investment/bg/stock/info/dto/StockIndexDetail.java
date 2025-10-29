package com.vv.finance.investment.bg.stock.info.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StockIndexDetail
 * @Deacription 指数快照数据
 * @Author lh.sz
 * @Date 2021年11月17日 16:17
 **/
@Data
@ToString
@Builder
public class StockIndexDetail implements Serializable {
    private static final long serialVersionUID = -1;
    @ApiModelProperty("股票ID")
    private Long stockId;
    @ApiModelProperty(value = "日期")
    private Long time;
    @ApiModelProperty(value = "指数代码")
    private String indexCode;
    @ApiModelProperty(value = "指数名称")
    private String indexName;
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;

}
