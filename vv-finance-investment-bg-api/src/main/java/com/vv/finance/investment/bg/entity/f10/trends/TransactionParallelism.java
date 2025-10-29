package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 15:57
 * @Version 1.0
 * 交易并行
 */
@Data
@Builder
@ToString
public class TransactionParallelism implements Serializable {

    private static final long serialVersionUID = -4477428350577848165L;
    @ApiModelProperty(value = "代码")
    private String stockCode;
    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "并行证券代码")
    private String securitiesCode;
    @ApiModelProperty(value = "并行证券名称")
    private String securitiesName;
    @ApiModelProperty(value = "并行买卖单位")
    private BigDecimal unit;
    @ApiModelProperty(value = "并行原因")
    private String reason;
    @ApiModelProperty(value = "并行开始日")
    private Long startDate;
    @ApiModelProperty(value = "并行暂停买卖日")
    private String suspendDate;
    @ApiModelProperty(value = "并行买卖日")
    private String tradingDay;
}
