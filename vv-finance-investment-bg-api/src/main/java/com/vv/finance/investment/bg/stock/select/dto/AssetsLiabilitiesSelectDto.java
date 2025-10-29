package com.vv.finance.investment.bg.stock.select.dto;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 策略选股资产负债
 *
 * @author wsliang
 * @date 2022/1/19 15:22
 **/
@Data
public class AssetsLiabilitiesSelectDto implements Serializable {
    private static final long serialVersionUID = -2580043237782490172L;

    @ApiModelProperty(value = "流动资产")
    private F10Val currentAsset;

    @ApiModelProperty(value = "非流动资产")
    private F10Val nonCurrentAssets;

    @ApiModelProperty(value = "流动负债")
    private F10Val currentLiabilities;

    @ApiModelProperty(value = "非流动负债")
    private F10Val nonCurrentLiability;

    @ApiModelProperty(value = "总负债")
    private F10Val totalLiabilities;

    @ApiModelProperty(value = "股东权益")
    private F10Val stockholdersEquity;

    @ApiModelProperty(value = "固定资产")
    private F10Val fixedAssets;

    @ApiModelProperty(value = "应收账款")
    private F10Val accountsReceivable;

    @ApiModelProperty(value = "存货")
    private F10Val inventory;

    @ApiModelProperty(value = "商誉及无形资产")
    private F10Val goodwillAndIntangibleAssets;

    @ApiModelProperty(value = "总资产")
    private F10Val totalAssets;

    @ApiModelProperty(value = "资本储备")
    private F10Val capitalReserve;
}
