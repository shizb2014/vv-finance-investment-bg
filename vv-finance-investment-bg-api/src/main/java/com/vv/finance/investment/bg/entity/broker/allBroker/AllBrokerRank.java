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
public class AllBrokerRank implements Serializable {
    private static final long serialVersionUID = 8618929186251084021L;
    @ApiModelProperty(value = "编号")
    private String brokerId;
    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;
    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;
    @ApiModelProperty(value = "持股市值变动")
    private BigDecimal changeMarketValue;
    @ApiModelProperty(value = "市值变动比例")
    private BigDecimal changeMarketValueRatio;
    @ApiModelProperty(value = "持股量变动")
    private BigDecimal changeInShareholding;
    @ApiModelProperty(value = "排名")
    private int rank;

    @ApiModelProperty(value = "数据更新时间")
    private Long date;
}
