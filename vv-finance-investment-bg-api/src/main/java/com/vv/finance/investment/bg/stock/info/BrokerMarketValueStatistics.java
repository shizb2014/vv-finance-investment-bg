package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName: BrokerStatistics
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/23   10:03
 */

/**
 * 股票码表
 *
 * @author hamilton
 */
@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "t_broker_market_value_statistics")
@AllArgsConstructor
public class BrokerMarketValueStatistics implements Serializable {

    private static final long serialVersionUID = 8504135316106445028L;
    @TableField("F001D")
    private Long f001d;
    @TableField("broker_id")
    private String brokerId;
    @TableField("broker_held_number")
    private BigDecimal brokerHeldNumber;
    @TableField("market_val")
    private BigDecimal marketVal;
    @TableField("Create_Date")
    private Date createDate;

}