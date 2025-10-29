package com.vv.finance.investment.bg.entity.f10.estimation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName EstimationAnalyzeVal
 * @Deacription 估值分析val
 * @Author lh.sz
 * @Date 2021年08月17日 16:37
 **/
@Data
@ToString
@Builder
public class EstimationAnalyzeVal implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    /**
     * 当前值
     */
    @ApiModelProperty(value = "当前值")
    private BigDecimal nowValue;

    /**
     * 分位点
     */
    @ApiModelProperty(value = "分位点")
    private BigDecimal percentValue;

    /**
     * 80%分位值
     */
    @ApiModelProperty(value = "80%分位值")
    private BigDecimal eightyPercentValue;

    /**
     * 50%分位值
     */
    @ApiModelProperty(value = "50%分位值")
    private BigDecimal fiftyPercentValue;

    /**
     * 20%分位值
     */
    @ApiModelProperty(value = "20%分位值")
    private BigDecimal twentyPercentValue;

    /**
     * 最大值
     */
    @ApiModelProperty(value = "最大值")
    private BigDecimal max;

    /**
     * 最小值
     */
    @ApiModelProperty(value = "最小值")
    private BigDecimal min;

    /**
     * 平均值
     */
    @ApiModelProperty(value = "平均值")
    private BigDecimal avg;

    /**
     * 标准差
     */
    @ApiModelProperty(value = "标准差")
    private BigDecimal standardDeviation;

}
