package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName RatingsTableEntity
 * @Deacription 主要指标图表
 * @Author lh.sz
 * @Date 2021年07月23日 14:29
 **/
@Data
@ToString
public class RatingsTableEntity implements Serializable {


    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "每股盈利")
    private F10Val earningPerShare;

    @ApiModelProperty(value = "每股净资产")
    private F10Val netAssetPerShare;

    @ApiModelProperty(value = "每股现金流")
    private F10Val cashFlowPerShare;

    @ApiModelProperty(value = "营业收入")
    private F10Val operatingRevenue;

    @ApiModelProperty(value = "资产回报率")
    private F10Val roa;

    @ApiModelProperty(value = "股东权益回报率")
    private F10Val roe;

    @ApiModelProperty(value = "净利润")
    private F10Val netProfits;

    @ApiModelProperty(value = "净利率")
    private F10Val netProfitRatio;

    @ApiModelProperty(value = "货币单位")
    private String monetaryUnit;

    @ApiModelProperty(value = "日期")
    private Long time;
}
