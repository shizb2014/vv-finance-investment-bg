package com.vv.finance.investment.bg.dto.Indicator;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/30 18:07
 */
@Data
public class KDJIndicatorDTO extends BaseIndicatorDTO{
    private BigDecimal k;
    private BigDecimal d;
    private BigDecimal j;
}
