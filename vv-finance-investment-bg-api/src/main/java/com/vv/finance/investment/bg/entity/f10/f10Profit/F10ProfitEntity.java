package com.vv.finance.investment.bg.entity.f10.f10Profit;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/24 11:41
 * @Version 1.0
 */
@ToString
@Data
public class F10ProfitEntity implements Serializable {

    private static final long serialVersionUID = -4432972598046704740L;

    @ApiModelProperty(value = "净利润")
    private F10Val netProfits;
    @ApiModelProperty(value = "经营溢利")
    private F10Val operatingProfit;
    @ApiModelProperty(value = "营业收入")
    private F10Val taking;
    @ApiModelProperty(value = "时间日期")
    private Long time;

    @ApiModelProperty("币种 英文简称")
    private String currency;

    public F10ProfitEntity() {
    }
}
