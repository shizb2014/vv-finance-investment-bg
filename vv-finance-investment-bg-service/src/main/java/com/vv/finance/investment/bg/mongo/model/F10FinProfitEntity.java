package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName F10ProfitEntity
 * @Deacription 利润表(金融)
 * @Author lh.sz
 * @Date 2021年07月20日 15:59
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_profit_financial")
public class F10FinProfitEntity extends F10EntityBase{


    private static final long serialVersionUID = -8900847415673069202L;
    @ApiModelProperty(value = "营业总收入")
    private F10Val grossRevenue;
    @ApiModelProperty(value = "保险索赔净额")
    private F10Val netInsuranceClaim;
    @ApiModelProperty(value = "利息净收入")
    private F10Val netInterestIncome;
    @ApiModelProperty(value = "利息收入")
    private F10Val interestIncome;
    @ApiModelProperty(value = "利息支出")
    private F10Val interestExpense;
    @ApiModelProperty(value = "费用净收入")
    private F10Val netExpenseIncome;
    @ApiModelProperty(value = "费用收益")
    private F10Val feeIncome;
    @ApiModelProperty(value = "费用支出")
    private F10Val feeExpense;
    @ApiModelProperty(value = "交易收入净额")
    private F10Val netTransactionRevenue;
    @ApiModelProperty(value = "保险收入净额")
    private F10Val netInsuranceIncome;
    @ApiModelProperty(value = "其他营业收入")
    private F10Val otherOperatingRevenue;
    @ApiModelProperty(value = "营业收入（不含贷款损失）")
    private F10Val operatingIncome;
    @ApiModelProperty(value = "贷款损失减值")
    private F10Val loanLossImpairment;
    @ApiModelProperty(value = "营业收入净额")
    private F10Val netOperatingIncome;
    @ApiModelProperty(value = "营业总成本")
    private F10Val totalOperatingExpenses;
    @ApiModelProperty(value = "雇员薪酬及福利")
    private F10Val employeeCompensationBenefits;
    @ApiModelProperty(value = "物业、机器及设备折旧")
    private F10Val depreciationPropertyMachineryEquipment;
    @ApiModelProperty(value = "无形资产摊销")
    private F10Val amortizationIntangibleAssets;
    @ApiModelProperty(value = "其他营业支出")
    private F10Val otherOperatingExpenditure;
    @ApiModelProperty(value = "营业利润")
    private F10Val operatingProfit;
    @ApiModelProperty(value = "其他非经营项目")
    private F10Val otherNonBusinessItems;
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
