package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSearch implements Serializable {
    private static final long serialVersionUID = 5919577900177514151L;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "股票名称")
    private String stockName;
}
