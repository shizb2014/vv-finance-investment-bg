package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/30 11:18
 */
@Data
public class BuySellOrderDTO implements Serializable {

    private static final long serialVersionUID = 5843915510876497802L;
    /**
     * 委托价格
     */
    private BigDecimal price;

    /**
     * 委托量
     */
    private Integer qty;

    /**
     * 挂单数量
     */
    private Integer num;
}
