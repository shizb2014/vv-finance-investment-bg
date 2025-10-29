package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerRedisField implements Serializable {
    private static final long serialVersionUID = 870752276519741659L;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "净买卖")
    private BigDecimal netTrade;
    @ApiModelProperty(value = "持股数量")
    private BigDecimal numberOfHolding;
    @ApiModelProperty(value = "持股比例(占流通股)")
    private BigDecimal holdingRatioInFlowShares;
    @ApiModelProperty(value = "持股比例(占已发行普通股)")
    private BigDecimal holdingRatioInOrdinaryShares;
    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;
}
