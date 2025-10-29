package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName F10CashCharEntity
 * @Deacription F10现金流量图表
 * @Author lh.sz
 * @Date 2021年07月29日 14:02
 **/
@Data
@ToString
public class F10CashCharEntity implements Serializable {

    private static final long serialVersionUID = -1883900656918954983L;

    @ApiModelProperty(value = "经营现金流")
    private F10Val operationalCashFlow;

    @ApiModelProperty(value = "投资现金流")
    private F10Val investingCashFlow;

    @ApiModelProperty(value = "融资现金流")
    private F10Val financingCashFlow;

    @ApiModelProperty(value = "时间日期")
    private Long time;

    @ApiModelProperty("币种 英文简称")
    private String currency;
}
