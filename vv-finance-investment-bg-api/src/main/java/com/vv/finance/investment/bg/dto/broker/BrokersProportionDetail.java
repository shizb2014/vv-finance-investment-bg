package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrokersProportionDetail implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "持股日期")
    private Long f001d;

    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;

    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;

    @ApiModelProperty(value = "参与者持股数量百分比")
    private BigDecimal f004n;

    /*

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal increase;

    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;

    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;

     */

}
