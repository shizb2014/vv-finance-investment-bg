package com.vv.finance.investment.bg.entity.f10.fintable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName F10BalanceSheetTable
 * @Deacription 资产负债表(非金融)
 * @Author lh.sz
 * @Date 2021年07月24日 10:52
 **/
@Data
@ToString
public class F10BalanceSheetNoFinTable implements Serializable {
    private static final long serialVersionUID = -1883900656918954983L;

    @ApiModelProperty(value = "报告类型")
    private String reportType;

    @ApiModelProperty(value = "总资产")
    private BigDecimal totalAssets;

    @ApiModelProperty(value = "流动资产")
    private BigDecimal currentAsset;

    @ApiModelProperty(value = "非流动资产")
    private BigDecimal nonCurrentAssets;

    @ApiModelProperty(value = "总负债")
    private BigDecimal totalLiabilities;

    @ApiModelProperty(value = "流动负债")
    private BigDecimal currentLiabilities;

    @ApiModelProperty(value = "非流动负债")
    private BigDecimal nonCurrentLiability;

    @ApiModelProperty(value = "资产净值")
    private BigDecimal netAssetValue;

    @ApiModelProperty(value = "总权益及非流动负债")
    private BigDecimal totalEquityAndNonCurrentLiabilities;

    @ApiModelProperty(value = "总权益")
    private BigDecimal totalEquity;

    @ApiModelProperty(value = "股东权益")
    private BigDecimal stockholdersEquity;

    @ApiModelProperty("时间")
    private Long time;
}
