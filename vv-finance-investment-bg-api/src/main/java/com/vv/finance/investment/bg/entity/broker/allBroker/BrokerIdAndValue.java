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
public class BrokerIdAndValue implements Serializable {
    private static final long serialVersionUID = 7125202062033907044L;
    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;
    @ApiModelProperty(value = "数值")
    private BigDecimal value;

}
