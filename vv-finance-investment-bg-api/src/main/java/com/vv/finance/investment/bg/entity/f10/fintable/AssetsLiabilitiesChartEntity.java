package com.vv.finance.investment.bg.entity.f10.fintable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/25 12:23
 * @Version 1.0
 */
@Data
@ToString
public class AssetsLiabilitiesChartEntity implements Serializable {

    private static final long serialVersionUID = 2390599497150425168L;

    @ApiModelProperty(value = "总资产")
    private BigDecimal totalAssets;

    @ApiModelProperty(value = "总负债")
    private BigDecimal totalIndebtedness;

    @ApiModelProperty(value = "负债率")
    private BigDecimal debtRatio;

    @ApiModelProperty(value = "时间日期")
    private Long time;

    @ApiModelProperty("币种 英文简称")
    private String currency;
}
