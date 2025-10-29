package com.vv.finance.investment.bg.entity.f10.industry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName EstimationVal
 * @Deacription 估值对比
 * @Author lh.sz
 * @Date 2021年08月17日 16:02
 **/
@Data
@ToString
@Builder
public class Estimation implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "排名")
    private Integer rank;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "市盈率TTM")
    private BigDecimal peTtm;

    @ApiModelProperty(value = "市盈率LYR")
    private BigDecimal peLyr;

    @ApiModelProperty(value = "周息率TTM")
    private BigDecimal dividendYieldTtm;

    @ApiModelProperty(value = "周息率LYR")
    private BigDecimal dividendYieldLyr;

    @ApiModelProperty(value = "市净率")
    private BigDecimal pb;

    @ApiModelProperty(value = "市盈增长比率（PEG）")
    private BigDecimal prg;

    @ApiModelProperty(value = "股东权益回报率（ROE）")
    private BigDecimal roe;

    @ApiModelProperty(value = "资产回报率（ROA）")
    private BigDecimal roa;

    @ApiModelProperty(value = "1:股票 2:行业平均值 3:行业中值")
    private Integer type;

    @ApiModelProperty(value = "更新日期")
    private String updateDate;

    @ApiModelProperty("股票类型")
    private Integer stockType;

    @ApiModelProperty("区域代码类型")
    private Integer regionType;
}
