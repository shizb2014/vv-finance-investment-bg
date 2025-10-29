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
 * 5分钟排行榜
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_5min_ranking")
@ApiModel(value="Stock5minRanking对象", description="5分钟排行榜")
public class Stock5minRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "行情时间")
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

    @ApiModelProperty(value = "五分钟变化，与kind对应")
    private BigDecimal min5Pct;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建时间")
    private String createBy;


}
