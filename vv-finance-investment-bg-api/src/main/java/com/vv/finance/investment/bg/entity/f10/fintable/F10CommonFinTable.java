package com.vv.finance.investment.bg.entity.f10.fintable;

import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName F10FinancialTable
 * @Deacription 财务报表
 * @Author lh.sz
 * @Date 2021年07月24日 10:40
 **/
@Data
@ToString
public class F10CommonFinTable implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "币种")
    private String currency;
    @ApiModelProperty(value = "利润表")
    private Object profit;
    @ApiModelProperty(value = "现金流量表")
    private Object cashFlow;
    @ApiModelProperty(value = "资产负债表")
    private Object balanceSheet;
    @ApiModelProperty(value = "行业类型")
    private F10MarketTypeEnum marketType;
}
