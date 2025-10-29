package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BrokersDetail implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;

    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;

    @ApiModelProperty(value = "持股变动数")
    private BigDecimal shareholdChange;

    @ApiModelProperty(value = "持股变动比例（占已发行普通股）")
    private BigDecimal shareholdChangeOfCirculation;

    @ApiModelProperty(value = "持股数")
    private BigDecimal shareHeld;

    @ApiModelProperty(value = "持股比例（占流通股）")
    private BigDecimal shareholdingRatioOfCirculation;

    @ApiModelProperty(value = "持股比例（占已发行普通股）")
    private BigDecimal shareholdingRatioOfIssue;

    @ApiModelProperty(value = "总持股比例")
    private BigDecimal totalHolding;

    @ApiModelProperty(value = "数据更新时间")
    private Long date;

}
