package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockToRank implements Serializable {
    private static final long serialVersionUID = 1507795738397862770L;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "排序字段")
    private BigDecimal rankValue;

}
