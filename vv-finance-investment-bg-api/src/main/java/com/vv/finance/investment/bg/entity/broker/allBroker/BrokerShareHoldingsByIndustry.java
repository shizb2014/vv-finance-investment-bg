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
public class BrokerShareHoldingsByIndustry implements Serializable {


    private static final long serialVersionUID = -1908271576001326993L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "行业代码")
    private String industryId;

    @ApiModelProperty(value="行业名称")
    private String industryName;

    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;

    @ApiModelProperty(value = "近一日持股市值变化")
    private BigDecimal recentChangeMarketValue;

    @ApiModelProperty(value="占总市值比例")
    private BigDecimal rationOfTotalMarketValue;


}
