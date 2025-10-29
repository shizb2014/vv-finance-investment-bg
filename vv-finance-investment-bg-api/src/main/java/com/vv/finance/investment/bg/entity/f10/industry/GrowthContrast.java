package com.vv.finance.investment.bg.entity.f10.industry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName GrowthContrast
 * @Deacription 成长性对比
 * @Author lh.sz
 * @Date 2021年08月19日 14:34
 **/
@Data
@ToString
@Builder
public class GrowthContrast implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "每股盈利增长率")
    private BigDecimal earningsPerShareGrowthRate;

    @ApiModelProperty(value = "营业收入增长率")
    private BigDecimal mbrg;

    @ApiModelProperty(value = "经营溢利增长率")
    private BigDecimal operatingProfitGrowthRate;

    @ApiModelProperty(value = "总资产增长率")
    private BigDecimal growthRateTotalAssets;

    @ApiModelProperty(value = "1:股票2:行业中值或行业平均值")
    private Integer type;
    
    @ApiModelProperty(value = "更新日期")
    private String updateDate;
}
