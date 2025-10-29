package com.vv.finance.investment.bg.entity.f10.estimation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName EstimationRadarChar
 * @Deacription 估值分析雷达图
 * @Author lh.sz
 * @Date 2021年09月06日 11:41
 **/
@Data
@ToString
public class EstimationRadarChar implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;
    @ApiModelProperty(value = "盈利能力")
    private EstimationRadar profit;

    @ApiModelProperty(value = "成长能力")
    private EstimationRadar growth;

    @ApiModelProperty(value = "运营能力")
    private EstimationRadar operating;

    @ApiModelProperty(value = "偿债能力")
    private EstimationRadar debt;

    @ApiModelProperty(value = "现金能力")
    private EstimationRadar cash;
}
