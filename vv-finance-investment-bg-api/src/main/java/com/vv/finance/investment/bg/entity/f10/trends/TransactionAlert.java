package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 15:46
 * @Version 1.0
 * 交易警报
 */
@Data
@Builder
@ToString
public class TransactionAlert implements Serializable {

    private static final long serialVersionUID = -4720349850320028034L;
    @ApiModelProperty(value = "公布日期")
    private String stockCode;
    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "警报类别")
    private String alertType;
    @ApiModelProperty(value = "警报代码")
    private String alertCode;
    @ApiModelProperty(value = "警报日暗盘收市价")
    private BigDecimal closingPrice;
    @ApiModelProperty(value = "较上日股价变动")
    private BigDecimal change;
    @ApiModelProperty(value = "警报日成交量")
    private BigDecimal turnover;
}
