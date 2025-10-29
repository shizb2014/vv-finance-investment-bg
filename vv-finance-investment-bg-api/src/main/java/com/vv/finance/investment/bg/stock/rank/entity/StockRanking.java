package com.vv.finance.investment.bg.stock.rank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 股票排行榜
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_ranking")
@ApiModel(value="StockRanking对象", description="股票排行榜")
public class StockRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "行业时间")
    private LocalDateTime mktTime;

    @ApiModelProperty(value = "股票代码")
    private String symbol;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty(value = "最新价")
    private BigDecimal last;

    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "成交量")
    private BigDecimal volume;

    @ApiModelProperty(value = "成交额")
    private BigDecimal amount;

    @ApiModelProperty(value = "振幅")
    private BigDecimal swing;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;


}
