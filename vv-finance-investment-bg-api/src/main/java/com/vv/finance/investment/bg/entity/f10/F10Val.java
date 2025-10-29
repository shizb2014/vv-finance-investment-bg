package com.vv.finance.investment.bg.entity.f10;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/7/19 17:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F10Val implements Serializable {
    private static final long serialVersionUID = -4713743216606447903L;
    /**
     * 值
     */
    private BigDecimal val;
    /**
     * 同比
     */
    private BigDecimal yoy;
}
