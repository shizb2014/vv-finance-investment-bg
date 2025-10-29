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
public class BrokerMarketValueTrend implements Serializable {
    private static final long serialVersionUID = -2407247122658522247L;
    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;
    @ApiModelProperty(value = "持股市值变动")
    private BigDecimal changeMarketValue;
    @ApiModelProperty(value = "持股量变动")
    private BigDecimal changeInShareholding;
    @ApiModelProperty(value = "恒生指数")
    private BigDecimal hangSengIndex;
    @ApiModelProperty(value = "恒生指数涨跌幅")
    private BigDecimal hangSengChgPct;
    @ApiModelProperty(value = "日期")
    private Long date;
}
