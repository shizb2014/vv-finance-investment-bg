package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 股票行业
 *
 * @author MI
 * @date 2024/05/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockIndustry implements Serializable {

    private static final long serialVersionUID = 4121620955804073489L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票code")
    private String stockCode;

    @ApiModelProperty(value = "股票名称")
    private String stockName;

    @ApiModelProperty(value = "首字母")
    private String firstRym;

    @ApiModelProperty(value = "行业代码")
    private Long industryId;

    @ApiModelProperty(value = "行业code")
    private String industryCode;

    @ApiModelProperty(value = "行业名称")
    private String industryName;

    @ApiModelProperty(value = "平均涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "总成交量")
    private BigDecimal amount;

    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;

    @ApiModelProperty(value = "最新价")
    private BigDecimal last;

    @ApiModelProperty(value = "行情时间")
    private LocalDateTime mktTime;

}
