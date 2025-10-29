package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryMarketValue {
    @ApiModelProperty(value = "行业代码")
    private String industryCode;
    @ApiModelProperty(value = "行业名称")
    private String industryName;
    @ApiModelProperty(value = "数值")
    private BigDecimal val;
    @ApiModelProperty(value = "日期")
    private Long date;
}
