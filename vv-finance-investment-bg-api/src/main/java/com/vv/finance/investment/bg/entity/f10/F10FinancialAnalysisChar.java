package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName F10FinancialAnalysisChar
 * @Deacription 财务分析
 * @Author lh.sz
 * @Date 2021年07月29日 15:05
 **/
@Data
@ToString
public class F10FinancialAnalysisChar implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;
    @ApiModelProperty(value = "盈利")
    private String[] profit;

    @ApiModelProperty(value = "成长")
    private String[] growth;

    @ApiModelProperty(value = "运营")
    private String[] operating;

    @ApiModelProperty(value = "偿债")
    private String[] debt;

    @ApiModelProperty(value = "现金")
    private String[] cash;

    private Long time;

}
