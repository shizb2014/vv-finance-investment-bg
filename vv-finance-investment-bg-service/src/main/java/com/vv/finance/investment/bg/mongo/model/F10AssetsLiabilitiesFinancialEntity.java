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
 * @Date: 2021/7/22 11:55
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_assets_liabilities_financial")
@EqualsAndHashCode
public class F10AssetsLiabilitiesFinancialEntity extends F10EntityBase {

    private static final long serialVersionUID = 9118787693713268461L;


    @ApiModelProperty(value = "总资产")
    private F10Val totalAssets;
    @ApiModelProperty(value = "库存现金及短缺资金")
    private F10Val cashAndShortTermFunds;
    @ApiModelProperty(value = "向其他银行托收中之项目")
    private F10Val collectionOfItemsFromOtherBank;
    @ApiModelProperty(value = "银行同业及其他金融机构存款")
    private F10Val depositsInInterbankAndOtherFinancialInstitutions;
    @ApiModelProperty(value = "香港政府负债证明书")
    private F10Val hongKongGovermentCertificateOfIndebtedness;
    @ApiModelProperty(value = "商业票据")
    private F10Val commercialPaper;
    @ApiModelProperty(value = "交易用途资产")
    private F10Val assetsForTradingPurposes;
    @ApiModelProperty(value = "非交易用途资产")
    private F10Val assetsForNonTradingPurposes;
    @ApiModelProperty(value = "按公平值入损益金融资产")
    private F10Val gainAndLoseFinancialAssetsAtFairValue;
    @ApiModelProperty(value = "可供出售金融资产")
    private F10Val availableForSaleFinancialAssets;
    @ApiModelProperty(value = "衍生性金融资产")
    private F10Val derivativeFinancialAssets;
    @ApiModelProperty(value = "所持存款证")
    private F10Val certificateOfDepositHeld;
    @ApiModelProperty(value = "银行同业贷款及垫款")
    private F10Val interbankLoansAndAdvances;
    @ApiModelProperty(value = "客户贷款及垫款")
    private F10Val customerLoansAndAdvances;
    @ApiModelProperty(value = "金融投资")
    private F10Val financialInvestment;
    @ApiModelProperty(value = "持有到期投资")
    private F10Val holdToMaturityInvestment;
    @ApiModelProperty(value = "联营及合资公司权益")
    private F10Val jointVenturesAndJointVenturesInterets;
    @ApiModelProperty(value = "商誉及无形资产")
    private F10Val goodwillAndIntangibleAssets;
    @ApiModelProperty(value = "固定资产")
    private F10Val fiexedAssets;
    @ApiModelProperty(value = "其他资产")
    private F10Val otherAssets;
    @ApiModelProperty(value = "总负债")
    private F10Val totalLiabilities;
    @ApiModelProperty(value = "香港政府流通纸币")
    private F10Val hongKongGovernmentCirculatesPaperMoney;
    @ApiModelProperty(value = "向其他银行传送之项目")
    private F10Val itemsTransmittedToOtherBanks;
    @ApiModelProperty(value = "银行同业及金融机构存款（负债）")
    private F10Val depositsOfInterbankAndFinancialInstitutionsLiabilities;
    @ApiModelProperty(value = "定期存放银行同业及金融机构（负债）")
    private F10Val timeDepositsInterbankAndFinancialInstitutionsLiabilities;
    @ApiModelProperty(value = "客户存款")
    private F10Val customersDeposit;
    @ApiModelProperty(value = "已发行存款证")
    private F10Val certificatesOfDepositIssued;
    @ApiModelProperty(value = "已发行债券")
    private F10Val bondsIssued;
    @ApiModelProperty(value = "已发行可换股债券")
    private F10Val convertibleBondsIssued;
    @ApiModelProperty(value = "已发行浮息票据")
    private F10Val floatingRateNotesIssued;
    @ApiModelProperty(value = "交易用途负债")
    private F10Val tradingLiabilities;
    @ApiModelProperty(value = "按公平值入损益金融负债")
    private F10Val enteringProfitAndLossFinancialLiabilitiesAtFairValue;
    @ApiModelProperty(value = "衍生金融负债")
    private F10Val derivativeFinancialLiability;
    @ApiModelProperty(value = "后偿负债")
    private F10Val subordinatedDebt;
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
