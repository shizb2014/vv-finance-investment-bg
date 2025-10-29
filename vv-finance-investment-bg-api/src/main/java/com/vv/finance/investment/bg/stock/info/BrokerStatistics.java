package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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
@TableName(value = "t_broker_statistics")
public class BrokerStatistics implements Serializable {

    private static final long serialVersionUID = -7617744290170316694L;
    @TableField("SECCODE")
    private String seccode;
    @TableField("F001D")
    private Long f001d;
    @TableField("F002V")
    private String f002v;
    @TableField("F003N")
    private BigDecimal f003n;
    @TableField("F004N")
    private BigDecimal f004n;
    @TableField("F014V")
    private String f014v;
    @TableField("market_val")
    private BigDecimal marketVal;
    @TableField("end_price")
    private BigDecimal endPrice;
    @TableField("f003n_org")
    private BigDecimal f003nOrg;
    @TableField("end_price_org")
    private BigDecimal endPriceOrg;
    @TableField("create_date")
    private Date createDate;
    @TableField("modified_date")
    private Date modifiedDate;

}