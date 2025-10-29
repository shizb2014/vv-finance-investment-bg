package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Deacription 主要指标图表
 * @Author wsl
 * @Date 2021年9月9日
 **/
@Data
@ToString
public class RatingsDigestTableEntity implements Serializable {


    private static final long serialVersionUID = -5991011510333519399L;

    @ApiModelProperty(value = "主要指标 ")
    private KeyFiguresDigestVo keyFigures;

    @ApiModelProperty(value = "盈利能力 ")
    private ProfitabilityDigestVo profitability;

    @ApiModelProperty(value = "成长能力 ")
    private GrowthAbilityDigestVo growthAbility;

    @ApiModelProperty(value = "运营能力 ")
    private OperatingCapacityDigestVo operatingCapacity;

    @ApiModelProperty(value = "偿债能力 ")
    private SolvencyDigestVo solvency;

    @ApiModelProperty(value = "表现能力 ")
    private CashabilityDigestVo cashability;

    @ApiModelProperty(value = "每股指标 ")
    private PerShareIndicatorDigestVo perShareIndicator;

    @ApiModelProperty("类型 金融/非金融/保险")
    private Integer marketType;

    private Long time;

    @ApiModelProperty(value = "报告类型 I\n" +
            "F\n" +
            "Q1\n" +
            "Q3\n" +
            "P\n" +
            "Q4\n" +
            "Q5")
    private String reportType;
}
