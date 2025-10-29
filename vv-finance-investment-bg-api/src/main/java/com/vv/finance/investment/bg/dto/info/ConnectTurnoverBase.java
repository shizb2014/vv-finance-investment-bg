package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/4/22 17:19
 */
@Data
public class ConnectTurnoverBase implements Serializable {
    private static final long serialVersionUID = -5584806774326048208L;
    private Long time;
    private BigDecimal netTurnover;
    private String market;
    private String direction;
}
