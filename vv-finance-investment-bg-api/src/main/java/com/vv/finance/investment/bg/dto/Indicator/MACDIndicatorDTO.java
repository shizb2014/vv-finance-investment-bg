package com.vv.finance.investment.bg.dto.Indicator;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/30 17:57
 */
@Data
public class MACDIndicatorDTO extends BaseIndicatorDTO {


    private BigDecimal diff;
    private BigDecimal dea;
    private BigDecimal macd;
    private BigDecimal ema12;
    private BigDecimal ema26;
}
