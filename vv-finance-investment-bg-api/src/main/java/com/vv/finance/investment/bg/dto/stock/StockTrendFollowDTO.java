package com.vv.finance.investment.bg.dto.stock;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * TODO
 *
 * @author Liam
 * @version 1.0
 * @date 2020/11/13 15:42
 */
@Data
public class StockTrendFollowDTO implements Serializable {
    private static final long serialVersionUID = -6528022084546827011L;
    private BigDecimal hh;
    private BigDecimal lc;
    private BigDecimal hc;
    private BigDecimal ll;
}
