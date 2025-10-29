package com.vv.finance.investment.bg.dto.Indicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/30 18:10
 */
@Data
public class RSIIndicatorDTO extends BaseIndicatorDTO{

    @ApiModelProperty(value = "RSI指标")
    private BigDecimal upAverage;

    @ApiModelProperty(value = "RSI指标")
    private BigDecimal downAverage;

    @ApiModelProperty(value = "12日相对强弱指标")
    private BigDecimal rsi12;


}
