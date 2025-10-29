package com.vv.finance.investment.bg.stock.quotes;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/23 10:50
 */
@Data
@TableName("t_connect_turnover")
public class ConnectTurnover extends BaseEntity {

    private static final long serialVersionUID = 1211006001637197256L;
    /**
     * 协议
     */
    private String protocol;
    /**
     * 市场
     */
    private String market;
    /**
     * 交易方向
     */
    private String direction;
    /**
     * 时间
     */
    private String time;
    /**
     * 买入额
     */
    private BigDecimal buyturnover;
    /**
     * 卖出额
     */
    private BigDecimal sellturnover;
    /**
     * 买卖总额
     */
    private BigDecimal buysell;

}
