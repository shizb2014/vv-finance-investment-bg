package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerSearch implements Serializable {
    private static final long serialVersionUID = 2534838331064666371L;
    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;
    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;
}
