package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class TopBrokersProportionDetail implements Serializable {
    private static final long serialVersionUID = -4892924975697974671L;

    @ApiModelProperty(value = "持股日期")
    private Long f001d;

    /*

    @ApiModelProperty(value = "前5/10/20经纪商合计编号")
    private String brokerId;

    @ApiModelProperty(value = "前5/10/20经纪商合计名称")
    private String brokerName;

     */

    @ApiModelProperty(value = "今日前5/10/20经纪商持股比例之和")
    private BigDecimal topPercent;
}
