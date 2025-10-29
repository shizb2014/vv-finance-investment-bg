package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/22 14:13
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_assets_liabilities_insurance")
public class F10AssetsLiabilitiesInsuranceEntity extends F10EntityBase {
    private static final long serialVersionUID = -7666101279924983446L;

    @ApiModelProperty(value = "总资产")
    private F10Val totalAssets;
    @ApiModelProperty(value = "法定存款")
    private F10Val mandatoryDeposits;
    @ApiModelProperty(value = "现金及银行寄存")
    private F10Val cashAndBankDeposit;
    @ApiModelProperty(value = "债权类证券")
    private F10Val debtSecurities;
    @ApiModelProperty(value = "股权类证券")
    private F10Val equitySecurities;
    @ApiModelProperty(value = "衍生金融资产")
    private F10Val derivativeFinancialAssets;
    @ApiModelProperty(value = "投资物业")
    private F10Val investmentProperty;
    @ApiModelProperty(value = "房屋，产房及设备")
    private F10Val housingDeliveryRoomAndEquipment;
    @ApiModelProperty(value = "租赁土地权益")
    private F10Val interestInLeasedLand;
    @ApiModelProperty(value = "预付土地/租赁款项")
    private F10Val prepaidLandLeasePayments;
    @ApiModelProperty(value = "贷款")
    private F10Val loans;
    @ApiModelProperty(value = "其他投资")
    private F10Val otherInvestment;
    @ApiModelProperty(value = "联营公司权益")
    private F10Val interestInAssociates;
    @ApiModelProperty(value = "共同控制公司权益")
    private F10Val jcci;
    @ApiModelProperty(value = "商誉")
    private F10Val goodWill;
    @ApiModelProperty(value = "其他无形资产")
    private F10Val otherIntangibleAssets;
    @ApiModelProperty(value = "应收保险款项")
    private F10Val insuranceReceivable;
    @ApiModelProperty(value = "分保资产")
    private F10Val reinsuranceAssets;
    @ApiModelProperty(value = "应收关连公司款项")
    private F10Val arrc;
    @ApiModelProperty(value = "递延保单获取成本")
    private F10Val deferredPolicyAcquisitionCosts;
    @ApiModelProperty(value = "递延税项资产")
    private F10Val deferredTaxAssets;
    @ApiModelProperty(value = "其它资产")
    private F10Val otherAssets;
    @ApiModelProperty(value = "总负债")
    private F10Val totalLiabilities;
    @ApiModelProperty(value = "保险准备金")
    private F10Val insuranceReserve;
    @ApiModelProperty(value = "保险保障基金")
    private F10Val insuranceProtectionFond;
    @ApiModelProperty(value = "应付保险款项")
    private F10Val insurancePayable;
    @ApiModelProperty(value = "保户投资合同负债")
    private F10Val insuredInvestmentContractLiabilities;
    @ApiModelProperty(value = "卖出回购资产")
    private F10Val sellBuybackAssets;
    @ApiModelProperty(value = "衍生金融负债")
    private F10Val derivativeFinancialLiability;
    @ApiModelProperty(value = "应付关连公司款项")
    private F10Val amountsPayableToRelatedCompanies;
    @ApiModelProperty(value = "应付所得税")
    private F10Val incomeTaxPayable;
    @ApiModelProperty(value = "递延税项负债")
    private F10Val deferredTaxLiabilities;
    @ApiModelProperty(value = "其他负债")
    private F10Val otherDebt;
    @ApiModelProperty(value = "资产净值")
    private F10Val netAssetValue;
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
