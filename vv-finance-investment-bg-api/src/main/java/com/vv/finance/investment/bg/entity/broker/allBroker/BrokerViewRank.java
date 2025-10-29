package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerViewRank implements Serializable {
    private static final long serialVersionUID = 9098786194683137598L;
    @ApiModelProperty(value = "变动比例前5")
    private List<StockAndHoldingRatio> changeRatioTopFive;
    @ApiModelProperty(value = "持股比例前5")
    private List<StockAndHoldingRatio> holdingRatioTopFive;
    @ApiModelProperty(value = "变动市值前5")
    private List<StockAndHoldingRatio> changeMarketValueTopFive;
    @ApiModelProperty(value = "持股市值前5")
    private List<StockAndHoldingRatio> holdingMarketValueTopFive;

    @ApiModelProperty(value = "数据更新时间")
    private Long date;
}
