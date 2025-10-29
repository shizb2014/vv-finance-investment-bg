package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/18 11:03
 * @Version 1.0
 * 股权变动
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockHoldingRiseFall implements Serializable {
    private static final long serialVersionUID = 9077351615048463903L;

    /**
     * 公布日期
     */
    @ApiModelProperty(value = "日期")
    private Long date;

    /**
     * 持仓类型
     */
    @ApiModelProperty(value = "持仓类型")
    private String positionType;

    /**
     * 增减持金额
     */
    @ApiModelProperty(value = "增减持金额")
    private BigDecimal changeAmount;

    /**
     * 增持金额
     */
    @ApiModelProperty(value = "增持金额")
    private BigDecimal riseAmount;

    /**
     * 减持金额
     */
    @ApiModelProperty(value = "减持金额")
    private BigDecimal fallAmount;

    /**
     * k线日期
     */
    @ApiModelProperty(value = "k线日期")
    private Long klineDate;

    /**
     * 股价
     */
    @ApiModelProperty(value = "股价")
    private BigDecimal stockPrice;
}
