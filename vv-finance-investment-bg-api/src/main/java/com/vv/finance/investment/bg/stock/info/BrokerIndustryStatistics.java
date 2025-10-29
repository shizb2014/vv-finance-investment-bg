package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 经纪商行业维度表
 */
@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "t_broker_industry_statistics")
public class BrokerIndustryStatistics {
    @TableField("id")
    private String id;
    @TableField("f001d")
    private Long f001d;
    @TableField("broker_id")
    private String brokerId;
    @TableField("industry_name")
    private String industryName;
    @TableField("market_val")
    private BigDecimal marketVal;
    @TableField("create_date")
    private Date createDate;
}
