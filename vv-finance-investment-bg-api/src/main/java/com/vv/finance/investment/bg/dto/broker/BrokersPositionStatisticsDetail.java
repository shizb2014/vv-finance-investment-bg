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
public class BrokersPositionStatisticsDetail implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "持股日期")
    private Long f001d;

    //=======经纪商持仓===========
    @ApiModelProperty(value = "持股总数")
    private BigDecimal shareholdTotal;

    @ApiModelProperty(value = "总持股比例（占流通股）")
    private BigDecimal shareholdTotalRatio;

    @ApiModelProperty(value = "变动股数")
    private BigDecimal shareholdChange;

    @ApiModelProperty(value = "变动比例")
    private BigDecimal shareholdChangeRatio;


    //===========经纪商统计==========
    @ApiModelProperty(value = "总数")
    private BigDecimal brokersTotal;

    @ApiModelProperty(value = "新增")
    private Integer brokersAdd;

    @ApiModelProperty(value = "消失")
    private Integer brokersReduce;

    @ApiModelProperty(value = "变动")
    private BigDecimal brokersChange;

    @ApiModelProperty(value = "增持")
    private Integer brokersIncreaseQuantity;

    @ApiModelProperty(value = "增持股数")
    private BigDecimal brokersIncreaseSharehold;

    @ApiModelProperty(value = "减持")
    private Integer brokersSubtractQuantity;

    @ApiModelProperty(value = "减持股数")
    private BigDecimal brokersSubtractSharehold;



    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal increase;

    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;


    //=============参与者类别统计========
    @ApiModelProperty(value="机构托管商")
    private BigDecimal organizationShareHold;

    @ApiModelProperty(value="券商")
    private BigDecimal brokerShareHold;

//    @ApiModelProperty(value = "港股通(深)")
//    private BigDecimal szStockConnectShareHold;
//
//    @ApiModelProperty(value = "港股通(沪)")
//    private BigDecimal hkStockConnectShareHold;

    @ApiModelProperty(value = "其他中介")
    private BigDecimal otherIntermediaryShareHold;

    @ApiModelProperty(value="自愿披露投资者")
    private BigDecimal voluntaryInvestorsShareHold;

    @ApiModelProperty(value="不原披露投资者")
    private BigDecimal involuntaryInvestorsShareHold;


}
