package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/4/8 19:04
 */
@Data
public class CapitalFlowDTO implements Serializable {

    private static final long serialVersionUID = 7798335195342188422L;
    private Long time;

    private String timeStr;

    private BigDecimal capital;

}
