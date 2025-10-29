package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingAndOtherRevenue;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingCostsAndExpenses;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingProfit;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName F10ProfitEntity
 * @Deacription 利润表(非金融)
 * @Author lh.sz
 * @Date 2021年07月20日 15:59
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_profit_no_financial")
public class F10NoFinProfitEntity  extends F10EntityBase  {


    private static final long serialVersionUID = -3971063635803994750L;
    @ApiModelProperty(value = "营业总收入")
    private NoFinOperatingAndOtherRevenue operatingAndOtherRevenue;
    @ApiModelProperty(value = "营业成本及支出")
    private NoFinOperatingCostsAndExpenses operatingCostsAndExpenses;
    @ApiModelProperty(value = "营业利润")
    private NoFinOperatingProfit operatingProfit;
    @ApiModelProperty(value = "除税前溢利")
    private F10Val profitBeforeTaxation;
    @ApiModelProperty(value = "税项")
    private F10Val tax;
    @ApiModelProperty(value = "期内损益")
    private F10Val profitAndLossDuringThePeriod;
    @ApiModelProperty(value = "归母净利润")
    private F10Val holdersOfShareCapitalOfTheCompany;
    @ApiModelProperty(value = "普通股股东")
    private F10Val commonStockholder;
    @ApiModelProperty(value = "优先股股东")
    private F10Val preferredStockholder;
    @ApiModelProperty(value = "非控股权益")
    private F10Val nonControllingInterests;
    @ApiModelProperty(value = "其他权益持有人")
    private F10Val otherEquityHolders;
    @ApiModelProperty(value = "因换算海外业务所产生之汇兑差额")
    private F10Val tedaftcob;
    @ApiModelProperty(value = "期内全面损益总额")
    private F10Val toapldtp;
    @ApiModelProperty(value = "每股摊薄盈利")
    private F10Val dilutedEarningsPerShare;
    @ApiModelProperty(value = "每股基本盈利")
    private F10Val basicEarningsPerShare;
    @ApiModelProperty(value = "审计意见")
    private String auditOpinion;
    @ApiModelProperty(value = "审计费用")
    private F10Val auditFee;


}
