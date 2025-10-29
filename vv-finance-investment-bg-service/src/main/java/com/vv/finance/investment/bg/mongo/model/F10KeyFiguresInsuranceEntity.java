package com.vv.finance.investment.bg.mongo.model;

import com.vv.finance.investment.bg.entity.f10.insurance.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;


/**
 * @author hamilton
 * @date 2021/7/20 11:15
 * 保险指标
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "f10_key_figures_insurance")
public class F10KeyFiguresInsuranceEntity extends F10EntityBase {


    private static final long serialVersionUID = 7433505578907280508L;
    @ApiModelProperty(value = "主要指标 ")
    private KeyFiguresInsurance keyFigures;

    @ApiModelProperty(value = "盈利能力 ")
    private ProfitabilityInsurance profitability;

    @ApiModelProperty(value = "成长能力 ")
    private GrowthAbilityInsurance growthAbility;

    @ApiModelProperty(value = "运营能力 ")
    private OperatingCapacityInsurance operatingCapacity;

    @ApiModelProperty(value = "偿债能力 ")
    private SolvencyInsurance solvency;

    @ApiModelProperty(value = "变现能力 ")
    private CashabilityInsurance cashability;


    @ApiModelProperty(value = "每股指标 ")
    private PerShareIndicatorInsurance perShareIndicator ;



}
