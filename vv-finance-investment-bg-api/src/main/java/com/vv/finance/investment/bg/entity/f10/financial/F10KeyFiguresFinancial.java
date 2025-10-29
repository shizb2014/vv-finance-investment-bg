package com.vv.finance.investment.bg.entity.f10.financial;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author hamilton
 * @date 2021/7/19 16:31
 */
@Data
public class F10KeyFiguresFinancial implements Serializable {
    private static final long serialVersionUID = 8921545620613710839L;

    private String stockCode;
    @ApiModelProperty(value = "报告类型 I\n" +
            "F\n" +
            "Q1\n" +
            "Q3\n" +
            "P\n" +
            "Q4\n" +
            "Q5")
    private String reportType;
    @ApiModelProperty(value = "起始日期yyyy/MM/dd")
    private String startDate;
    @ApiModelProperty(value = "截止日期yyyy/MM/dd")
    private String endDate;
    @ApiModelProperty(value = "发布日期 yyyy/MM/dd")
    private String releaseDate;
    @ApiModelProperty(value = "起始日期时间戳")
    private Long startTimestamp;
    @ApiModelProperty(value = "截止日期时间戳")
    private Long endTimestamp;
    @ApiModelProperty(value = "发布日期时间戳ms")
    private Long releaseTimestamp;
    @ApiModelProperty(value = "币种 英文简称例如 HKD ")
    private String currency;
    @ApiModelProperty(value = "主要指标 ")
    private KeyFiguresFinancial keyFigures;

    @ApiModelProperty(value = "盈利能力 ")
    private ProfitabilityFinancial profitability;

    @ApiModelProperty(value = "成长能力 ")
    private GrowthAbilityFinancial growthAbility;

    @ApiModelProperty(value = "运营能力 ")
    private OperatingCapacityFinancial operatingCapacity;

    @ApiModelProperty(value = "偿债能力 ")
    private SolvencyFinancial solvency;

    @ApiModelProperty(value = "主要指标 ")
    private CashabilityFinancial cashability;


    @ApiModelProperty(value = "每股指标 ")
    private PerShareIndicatorFinancial perShareIndicator ;






}
