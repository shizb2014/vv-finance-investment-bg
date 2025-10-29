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
public class BrokerShareHoldingsByCode implements Serializable {

    private static final long serialVersionUID = 8360154871559365174L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value="股票代码")
    private String code;

    @ApiModelProperty(value="股票名称")
    private String stockName;

    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;

    @ApiModelProperty(value = "近一日持股市值变化")
    private BigDecimal recentChangeMarketValue;

    @ApiModelProperty(value="持股比例（占已发行普通股）")
    private BigDecimal holdRation;

    @ApiModelProperty(value="变动比例（占已发行普通股）")
    private BigDecimal holdChangeRation;

    @ApiModelProperty(value="占总市值比例")
    private BigDecimal rationOfTotalMarketValue;
}
