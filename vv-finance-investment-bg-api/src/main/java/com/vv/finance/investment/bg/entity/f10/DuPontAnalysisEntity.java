package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Deacription 杜邦分析
 * @Author wsl
 * @Date 2021年9月9日
 **/
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DuPontAnalysisEntity implements Serializable {

    private static final long serialVersionUID = 1136645683229248304L;

    @ApiModelProperty(value = "日期 ")
    private String date;

    @ApiModelProperty(value = "报告周期 ")
    private String reportPeriod;

    @ApiModelProperty(value = "净资产收益率 ")
    private BigDecimal netAssetProfitRatio;

    @ApiModelProperty(value = "资产负债率")
    private BigDecimal assetLiabilityRatio;

    @ApiModelProperty(value = "权益乘数")
    private BigDecimal equityMultiplier;

    @ApiModelProperty(value = "负债总额")
    private BigDecimal totalLiabilities;

    @ApiModelProperty(value = "资产总额")
    private BigDecimal totalAssets;

    @ApiModelProperty(value = "所有者权益总额")
    private BigDecimal totalOwnerEquity;

    @ApiModelProperty(value = "总资产收益率")
    private BigDecimal totalAssetProfitRatio;

    @ApiModelProperty(value = "销售净利率")
    private BigDecimal netSaleProfitRatio;

    @ApiModelProperty(value = "总资产周转率")
    private BigDecimal totalAssetTurnover;

    @ApiModelProperty(value = "净利润")
    private BigDecimal netProfit;

    @ApiModelProperty(value = "营业收入")
    private BigDecimal businessIncome;

    @ApiModelProperty(value = "币种")
    private String currencyType;
}
