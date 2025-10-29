package com.vv.finance.investment.bg.entity.f10.chart;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class F10SolvencyEntity implements Serializable {

    private static final long serialVersionUID = 8903094951837337937L;

    @ApiModelProperty(value = "产权比率")
    private String[] equityRatio;
    @ApiModelProperty(value = "权益乘数")
    private F10Val equityMultiplier;
    @ApiModelProperty(value = "偿债保障比率")
    private F10Val debtCoverageRatio ;
    @ApiModelProperty(value = "利息保障倍数")
    private F10Val interestCoverageRatio ;
    @ApiModelProperty(value = "经营现金流量比")
    private String[] operatingCashFlowRatio;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
