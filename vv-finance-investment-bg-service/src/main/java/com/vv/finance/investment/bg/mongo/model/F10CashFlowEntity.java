package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName F10CashFlowEntity
 * @Deacription 现金流量表
 * @Author lh.sz
 * @Date 2021年07月20日 15:59
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_cash_flow")
public class F10CashFlowEntity extends F10EntityBase {


    private static final long serialVersionUID = -3106876194278440929L;
    @ApiModelProperty(value = "经营活动现金流")
    private F10Val cashFlowFromeOperations;
    @ApiModelProperty(value = "除税前溢利")
    private F10Val profitBeforeTaxation;
    @ApiModelProperty(value = "利息净收入")
    private F10Val netInterestIncome;
    @ApiModelProperty(value = "利息收入")
    private F10Val interestIncome;
    @ApiModelProperty(value = "物业、机器及设备折旧")
    private F10Val dpme;
    @ApiModelProperty(value = "无形资产摊销")
    private F10Val amortizationIntangibleAssets;
    @ApiModelProperty(value = "折旧及摊销")
    private F10Val depreciationAmortization;
    @ApiModelProperty(value = "经营业务现金流")
    private F10Val cashFlowFromOperations;
    @ApiModelProperty(value = "已收利息")
    private F10Val receivedInterest;
    @ApiModelProperty(value = "已收股息")
    private F10Val receivedDividend;
    @ApiModelProperty(value = "已付税项")
    private F10Val paidTaxes;
    @ApiModelProperty(value = "投资活动现金流")
    private F10Val cashFlowFromInvestmentActivities;
    @ApiModelProperty(value = "添加固定资产")
    private F10Val additionFixdedAssets;
    @ApiModelProperty(value = "投资增加")
    private F10Val increasedInvestment;
    @ApiModelProperty(value = "出售固定资产")
    private F10Val saleOfPlantAssets;
    @ApiModelProperty(value = "投资减少")
    private F10Val lowerInvestment;
    @ApiModelProperty(value = "关联人士现金流")
    private F10Val cashFlowAssociate;
    @ApiModelProperty(value = "其它(投资)")
    private F10Val othersInvestment;
    @ApiModelProperty(value = "融资活动现金流")
    private F10Val cashFlowFromFinancingActivites;
    @ApiModelProperty(value = "新增贷款")
    private F10Val newBankLoans;
    @ApiModelProperty(value = "偿还贷款")
    private F10Val repayTheLoan;
    @ApiModelProperty(value = "已付利息")
    private F10Val interestPaid;
    @ApiModelProperty(value = "已派股息")
    private F10Val hasSentDividend;
    @ApiModelProperty(value = "其他（融资）")
    private F10Val othersFinancing;
    @ApiModelProperty(value = "汇率影响")
    private F10Val exchangeRateInfluence;
    @ApiModelProperty(value = "现金净额")
    private F10Val netCash;
    @ApiModelProperty(value = "期初现金")
    private F10Val initialCash;
    @ApiModelProperty(value = "期末现金")
    private F10Val finalCash;
    @ApiModelProperty(value = "审计意见")
    private String auditOpinion;



}
