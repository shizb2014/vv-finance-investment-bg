package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareholdingsTable implements Serializable {
    private static final long serialVersionUID = 2578031977679510764L;
    @ApiModelProperty("股票ID")
    private Long stockId;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "股票名称")
    private String stockName;
    @ApiModelProperty(value = "行业Id")
    private Long industryId;
    @ApiModelProperty(value = "行业code")
    private String industryCode;
    @ApiModelProperty(value = "行业名称")
    private String industryName;
    @ApiModelProperty(value = "持股变动股数")
    private BigDecimal changeInShareholding;
    @ApiModelProperty(value = "变动比例")
    private BigDecimal changeRatio;
    @ApiModelProperty(value = "持股数量")
    private BigDecimal numberOfHolding;
//    @ApiModelProperty(value = "持股比例(占流通股)")
//    private BigDecimal holdingRatioInFlowShares;
    @ApiModelProperty(value = "持股比例(占已发行普通股)")
    private BigDecimal holdingRatioInOrdinaryShares;
    @ApiModelProperty(value = "变动市值")
    private BigDecimal changeMarketValue;
    @ApiModelProperty(value = "参考持股市值")
    private BigDecimal referenceHoldingMarketValue;
    @ApiModelProperty(value = "持股日期")
    private Long holdingDate;
}
