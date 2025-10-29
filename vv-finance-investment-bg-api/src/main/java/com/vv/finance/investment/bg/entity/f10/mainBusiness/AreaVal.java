package com.vv.finance.investment.bg.entity.f10.mainBusiness;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/19 16:35
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class AreaVal implements Serializable {
    private static final long serialVersionUID = 2756459750470779308L;
    @ApiModelProperty(value = "地区")
    private String areaName;
    @ApiModelProperty(value = "营业额")
    private BigDecimal turnover;
    @ApiModelProperty(value = "占比")
    private BigDecimal pop;
    @ApiModelProperty(value = "总营业额")
    private BigDecimal sumTurnover;
    @ApiModelProperty(value = "币种单位 港元/人民币 等")
    private String currency;
}
