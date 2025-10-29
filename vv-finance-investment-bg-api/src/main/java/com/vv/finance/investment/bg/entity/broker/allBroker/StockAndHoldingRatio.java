package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAndHoldingRatio implements Serializable {
    private static final long serialVersionUID = -3337354257189225033L;
    @ApiModelProperty("股票ID")
    private Long stockId;
    @ApiModelProperty(value = "股票代码")
    String code;
    @ApiModelProperty(value = "股票名称")
    String stockName;
    @ApiModelProperty(value = "持股比例与日期")
    private List<ValueAndDate> valueAndDateList;
}
