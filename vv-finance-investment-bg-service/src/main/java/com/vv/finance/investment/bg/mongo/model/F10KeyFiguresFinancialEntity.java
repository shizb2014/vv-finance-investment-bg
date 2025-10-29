package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.financial.*;
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
 * 金融指标
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_key_figures_financial")
public class F10KeyFiguresFinancialEntity extends F10EntityBase {
    private static final long serialVersionUID = 8921545620613710839L;


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

    @ApiModelProperty(value = "变现能力 ")
    private CashabilityFinancial cashability;


    @ApiModelProperty(value = "每股指标 ")
    private PerShareIndicatorFinancial perShareIndicator;





}
