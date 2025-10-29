package com.vv.finance.investment.bg.entity.f10.chart;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ToString
@Data
public class F10ProfitabilityEntity implements Serializable{

    private static final long serialVersionUID = -2586547363070016207L;

    @ApiModelProperty(value = "收益指标")
    private String[] earningsIndicator;
    @ApiModelProperty(value = "销售利润比率")
    private String[] salesProfitRatio;
    @ApiModelProperty(value = "企业价值倍数")
    private String[] evebitda;

    @ApiModelProperty(value = "时间日期")
    private Long time;
}
