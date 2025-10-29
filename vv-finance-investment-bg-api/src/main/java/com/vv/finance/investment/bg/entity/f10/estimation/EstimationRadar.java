package com.vv.finance.investment.bg.entity.f10.estimation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName EstimationVal
 * @Deacription 估值分析雷达图Val
 * @Author lh.sz
 * @Date 2021年09月06日 11:49
 **/
@Data
@ToString
public class EstimationRadar implements Serializable {

    private static final long serialVersionUID = -9087750869374973077L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    /**
     * 股票val
     */
    @ApiModelProperty(value = "股票val")
    private BigDecimal stockVal;

    /**
     * 股票名称
     */
    @ApiModelProperty(value = "股票名称")
    private String stockName;

    /**
     * 股票代码
     */
    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    /**
     * 行业平均val
     */
    @ApiModelProperty(value = "行业平均val")
    private BigDecimal avgVal;

    /**
     * 分析维度
     */
    @ApiModelProperty("分析维度")
    private String dimension;
}
