package com.vv.finance.investment.bg.entity.f10.industry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName ScaleVal
 * @Deacription 规模对比
 * @Author lh.sz
 * @Date 2021年08月17日 16:19
 **/
@Data
@ToString
@Builder
public class Scale implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "总市值")
    private BigDecimal totalValue;

    @ApiModelProperty(value = "总资产")
    private BigDecimal totalAssets;

    @ApiModelProperty(value = "营业收入")
    private BigDecimal taking;

    @ApiModelProperty(value = "毛利")
    private BigDecimal grossMargin;

    @ApiModelProperty(value = "净利润")
    private BigDecimal retainedProfits;

    @ApiModelProperty(value = "1:股票2:行业中值或行业平均值")
    private Integer type;

    @ApiModelProperty(value = "更新日期")
    private String updateDate;

}
