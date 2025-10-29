package com.vv.finance.investment.bg.entity.f10.mainBusiness;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 16:18
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class BusVal implements Serializable {

    private static final long serialVersionUID = 7538292637055572721L;
    @ApiModelProperty(value = "业务名称")
    private String busName;
    @ApiModelProperty(value = "营业额")
    private BigDecimal turnover;
    @ApiModelProperty(value = "占比")
    private BigDecimal pop;
    @ApiModelProperty(value = "总营业额")
    private BigDecimal sumTurnover;
    @ApiModelProperty(value = "币种单位 港元/人民币 等")
    private String currency;
}
