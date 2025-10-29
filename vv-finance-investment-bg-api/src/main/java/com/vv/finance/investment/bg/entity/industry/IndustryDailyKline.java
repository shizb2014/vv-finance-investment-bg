package com.vv.finance.investment.bg.entity.industry;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2021-12-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_industry_daily_kline")
@ApiModel(value="IndustryDailyKline对象", description="")
public class IndustryDailyKline implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private Long time;

    private LocalDateTime timeStr;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal preClose;

    private BigDecimal amount;

    private BigDecimal volume;

    private BigDecimal turnoverRate;

    private BigDecimal chg;

    private BigDecimal chgPct;

    private BigDecimal swing;

    @ApiModelProperty("总市值")
    private BigDecimal totalMarket;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
