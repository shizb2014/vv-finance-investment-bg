package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName F10InsureProfitEntity
 * @Deacription 利润表(保险)
 * @Author lh.sz
 * @Date 2021年07月20日 15:59
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_profit_insure")
public class F10InsureProfitEntity extends F10EntityBase{


    private static final long serialVersionUID = -1638839196420586249L;
    @ApiModelProperty(value = "营业总收入")
    private F10Val grossRevenue;
    @ApiModelProperty(value = "已实现净保费收入")
    private F10Val realizedNetPremiumIncome;
    @ApiModelProperty(value = "净保费收入")
    private F10Val netPremiumIncome;
    @ApiModelProperty(value = "总保费收入")
    private F10Val totalPremium;
    @ApiModelProperty(value = "分出保费")
    private F10Val reinsurancePremium;
    @ApiModelProperty(value = "提取未到期责任准备金")
    private F10Val unearnedPremiumReserve;
    @ApiModelProperty(value = "净投资收益")
    private F10Val netInvestmentIncome;
    @ApiModelProperty(value = "投资收益")
    private F10Val investmentIncome;
    @ApiModelProperty(value = "汇兑收益")
    private F10Val exchangeGain;
    @ApiModelProperty(value = "其他营业收入")
    private F10Val otherOperatingRevenue;
    @ApiModelProperty(value = "保险支出及费用总额")
    private F10Val insuranceExpensesTotalExpenses;
    @ApiModelProperty(value = "赔款及保户利益")
    private F10Val indemnityInsuranceInterests;
    @ApiModelProperty(value = "延递保单成本")
    private F10Val deferredPolicyCosts;
    @ApiModelProperty(value = "佣金支出净额")
    private F10Val netCommissionExpenses;
    @ApiModelProperty(value = "管理费用")
    private F10Val administrationExpenses;
    @ApiModelProperty(value = "其他营业支出")
    private F10Val otherOperatingExpenditure;
    @ApiModelProperty(value = "营业利润")
    private F10Val operatingProfit;
    @ApiModelProperty(value = "融资成本")
    private F10Val financingCost;
    @ApiModelProperty(value = "共同控制及联营公司溢利")
    private F10Val pfjcac;
    @ApiModelProperty(value = "除税前溢利")
    private F10Val profitBeforeTaxation;
    @ApiModelProperty(value = "税项")
    private F10Val tax;
    @ApiModelProperty(value = "期内损益")
    private F10Val profitLossDuringPeriod;
    @ApiModelProperty(value = "归母净利润")
    private F10Val holdersShareCapitalCompany;
    @ApiModelProperty(value = "普通股股东")
    private F10Val commonStockholder;
    @ApiModelProperty(value = "优先股股东")
    private F10Val preferredStockholder;
    @ApiModelProperty(value = "非控股权益")
    private F10Val nonControllingInterests;
    @ApiModelProperty(value = "其他权益持有人")
    private F10Val otherEquityHolders;
    @ApiModelProperty(value = "每股摊薄盈利")
    private F10Val dilutedEarningsPerShare;
    @ApiModelProperty(value = "每股基本盈利")
    private F10Val basicEarningsPerShare;
    @ApiModelProperty(value = "审计意见")
    private String auditOpinion;
    @ApiModelProperty(value = "审计费用")
    private F10Val auditFee;



}
