package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author qinxi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryBrokerRankDTO implements Serializable {
    private static final long serialVersionUID = 8618929186251084021L;
    @ApiModelProperty(value = "编号")
    private String brokerId;
    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;
    @ApiModelProperty(value = "持股市值")
    private BigDecimal holdingMarketValue;
    @ApiModelProperty(value = "持股市值变动")
    private BigDecimal changeMarketValue;
}
