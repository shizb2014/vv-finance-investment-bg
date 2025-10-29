package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName:在计算top5集中度变动比例时，需要一个DTO对象保存股票code，前五经纪商持股比例之和，集中度变动比例
 */
@Data
public class TopFiveConcentrationCalculateDTO implements Serializable {

    private static final long serialVersionUID = -3696873191201096238L;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value ="top5/10持股比例之和" )
    private BigDecimal topConcentrationPercent;

    @ApiModelProperty(value ="集中度变动比例" )
    private BigDecimal concentrationTrend;
}
