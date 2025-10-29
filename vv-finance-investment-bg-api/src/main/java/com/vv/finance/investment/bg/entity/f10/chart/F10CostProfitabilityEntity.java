package com.vv.finance.investment.bg.entity.f10.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class F10CostProfitabilityEntity implements Serializable {

    private static final long serialVersionUID = 4648255271539938701L;

    @ApiModelProperty(value = "成本费用利润")
    private String[] costProfit;
    @ApiModelProperty(value = "主营成本占营收比例")
    private String[] majorCostRatio;
    @ApiModelProperty(value = "期间费用占营收比例")
    private String[] periodCostRatio;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
