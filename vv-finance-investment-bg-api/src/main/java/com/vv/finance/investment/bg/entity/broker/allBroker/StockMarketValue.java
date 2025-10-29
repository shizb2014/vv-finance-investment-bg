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
public class StockMarketValue implements Serializable {
    private static final long serialVersionUID = -6009752837049405000L;
    @ApiModelProperty(value = "行业名称")
    private String industryName;
    @ApiModelProperty(value = "持股市值")
    private BigDecimal marketValue;
}
