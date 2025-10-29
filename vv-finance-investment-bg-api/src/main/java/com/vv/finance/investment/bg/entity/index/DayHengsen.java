package com.vv.finance.investment.bg.entity.index;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 日恒生指数
 * @author heqianjiang
 */
@Data
public class DayHengsen implements Serializable {
    private  Long dateTime;
    private Double indexRate;

}
