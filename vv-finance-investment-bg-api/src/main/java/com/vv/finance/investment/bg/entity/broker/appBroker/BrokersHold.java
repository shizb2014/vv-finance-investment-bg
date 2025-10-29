package com.vv.finance.investment.bg.entity.broker.appBroker;

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
public class BrokersHold implements Serializable {
    private static final long serialVersionUID = -7700223122507685864L;


    @ApiModelProperty(value="经纪商名称")
    private String brokerName;

    @ApiModelProperty(value="持股比例")
    private BigDecimal holdRation;

    @ApiModelProperty(value="持股数量")
    private BigDecimal holdQuantity;

    @ApiModelProperty(value="变动比例")
    private BigDecimal holdChangeRation;

    @ApiModelProperty(value = "变动数量")
    private BigDecimal holdChangeQuantity;

    @ApiModelProperty(value = "总持股比例")
    private BigDecimal totalHolding;

    @ApiModelProperty(value = "数据更新时间")
    private Long date;
}
