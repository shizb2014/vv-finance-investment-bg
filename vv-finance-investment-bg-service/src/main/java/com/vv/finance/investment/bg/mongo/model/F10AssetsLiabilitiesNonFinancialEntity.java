package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/22 11:03
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_assets_liabilities_non_financial")
public class F10AssetsLiabilitiesNonFinancialEntity extends F10EntityBase  {

    private static final long serialVersionUID = 4901850881848285995L;

    @ApiModelProperty(value = "总资产")
    private F10Val totalAssets;
    @ApiModelProperty(value = "流动资产")
    private F10Val currentAsset;
    @ApiModelProperty(value = "存货")
    private F10Val inventory;
    @ApiModelProperty(value = "应收账款")
    private F10Val accountsReceivable;
    @ApiModelProperty(value = "金融资产")
    private F10Val financialAssets;
    @ApiModelProperty(value = "现金及银行结存")
    private F10Val cashAndBankBalances;
    @ApiModelProperty(value = "其他流动资产")
    private F10Val otherCurrentAsset;

    @ApiModelProperty(value = "非流动资产")
    private F10Val nonCurrentAssets;
    @ApiModelProperty(value = "固定资产")
    private F10Val fixedAssets;
    @ApiModelProperty(value = "投资")
    private F10Val investment;
    @ApiModelProperty(value = "商誉及无形资产")
    private F10Val goodwillAndIntangibleAssets;
    @ApiModelProperty(value = "其他非流动资产")
    private F10Val otherNonCurrentAssets;

    @ApiModelProperty(value = "总负债")
    private F10Val totalLiabilities;
    @ApiModelProperty(value = "流动负债")
    private F10Val currentLiabilities;
    @ApiModelProperty(value = "应付账款")
    private F10Val accountsPayable;
    @ApiModelProperty(value = "短期债项")
    private F10Val shortTermLiabilities;
    @ApiModelProperty(value = "其他短期负债")
    private F10Val otherShortTermLiabilities;
    @ApiModelProperty(value = "非流动负债")
    private F10Val nonCurrentLiability;
    @ApiModelProperty(value = "长期债项")
    private F10Val longTermLiabilities;
    @ApiModelProperty(value = "其他非流动负债")
    private F10Val otherNonCurrentLiabilities;

    @ApiModelProperty(value = "资产净值")
    private F10Val netAssetValue;
    @ApiModelProperty(value = "总权益及非流动负债")
    private F10Val totalEquityAndNonCurrentLiabilities;
    @ApiModelProperty(value = "总权益")
    private F10Val totalEquity;
    @ApiModelProperty(value = "股本总额")
    private F10Val totalCapital;
    @ApiModelProperty(value = "股本（普通股）")
    private F10Val capitalStockCommonStock;
    @ApiModelProperty(value = "股本（优先股）")
    private F10Val capitalStockPreferredStock;
    @ApiModelProperty(value = "股本溢价")
    private F10Val capitalStockPremium;
    @ApiModelProperty(value = "资本储备")
    private F10Val capitalReserve;
    @ApiModelProperty(value = "其他储备")
    private F10Val otherReserve;
    @ApiModelProperty(value = "保留溢利")
    private F10Val retainedProfit;
    @ApiModelProperty(value = "储备总额")
    private F10Val totalReserves;

    @ApiModelProperty(value = "股东权益")
    private F10Val stockholdersEquity;
    @ApiModelProperty(value = "非控股权益")
    private F10Val nonControllingInterests;
    @ApiModelProperty(value = "其他权益持有人")
    private F10Val otherEquityHolders;
    @ApiModelProperty(value = "审计意见")
    private String auditOpinion;


}
