package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/29 19:27
 */
@Data
public class TradeBaseDTO implements Serializable {

    /**
     * 时间
     */
    private String time;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private Integer num;

    /**
     * 买卖方向
     */
    private String direction;
}
