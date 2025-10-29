package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrokersPositionChangesDetail implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;

    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;

    @ApiModelProperty(value = "持股日期")
    private Long f001d;

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


    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal increase;

    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;

    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;

    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;


    @ApiModelProperty(value = "数据更新时间")
    private Long date;
}
