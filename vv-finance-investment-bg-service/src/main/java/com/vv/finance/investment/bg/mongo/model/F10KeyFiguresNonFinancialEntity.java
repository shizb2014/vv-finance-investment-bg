package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.nonfinancial.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;


/**
 * @author hamilton
 * @date 2021/7/19 16:31
 * 非金融指标
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_key_figures_non_financial")
public class F10KeyFiguresNonFinancialEntity  extends F10EntityBase  {

    private static final long serialVersionUID = 7919680388459091037L;
    @ApiModelProperty(value = "主要指标 ")
    private KeyFiguresNonFinancial keyFigures;

    @ApiModelProperty(value = "盈利能力 ")
    private ProfitabilityNonFinancial profitability;

    @ApiModelProperty(value = "成长能力 ")
    private GrowthAbilityNonFinancial growthAbility;

    @ApiModelProperty(value = "运营能力 ")
    private OperatingCapacityNonFinancial operatingCapacity;

    @ApiModelProperty(value = "偿债能力 ")
    private SolvencyNonFinancial solvency;

    @ApiModelProperty(value = "表现能力 ")
    private CashabilityNonFinancial cashability;


    @ApiModelProperty(value = "每股指标 ")
    private PerShareIndicatorNonFinancial perShareIndicator ;

    @ApiModelProperty(value = "现金流量指标")
    private CashFlowIndicator cashFlowIndicator;

    @ApiModelProperty(value = "成本盈利能力")
    private CostProfitability costProfitability;

}
