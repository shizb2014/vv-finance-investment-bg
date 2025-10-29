package com.vv.finance.investment.bg.stock.kline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author hamilton
 * @date 2020/10/29 10:59
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class StockKline extends BaseStockKlineEntity implements Serializable  {
    private static final long serialVersionUID = 6980407489955182004L;
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;

    @ApiModelProperty(value = "最高价")
    private BigDecimal high;

    @ApiModelProperty(value = "最低价")
    private BigDecimal low;

    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;

    @ApiModelProperty(value = "昨日收盘价")
    private BigDecimal preClose;


    @ApiModelProperty(value = "均线5")
    @TableField("ma_5")
    private BigDecimal ma5;

    @ApiModelProperty(value = "均线10")
    @TableField("ma_10")
    private BigDecimal ma10;

    @ApiModelProperty(value = "均线15")
    @TableField("ma_15")
    private BigDecimal ma15;
    @ApiModelProperty(value = "均线20")
    @TableField("ma_20")
    private BigDecimal ma20;
    @ApiModelProperty(value = "均线30")
    @TableField("ma_30")
    private BigDecimal ma30;
    @ApiModelProperty(value = "均线50")
    @TableField("ma_50")
    private BigDecimal ma50;

    @ApiModelProperty(value = "均线60")
    @TableField("ma_60")
    private BigDecimal ma60;

    @ApiModelProperty(value = "均线120")
    @TableField("ma_120")
    private BigDecimal ma120;

    @ApiModelProperty(value = "数据模式")
    private String mode;

    @ApiModelProperty(value = "复权方式")
    private String adjhkt;

    @ApiModelProperty(value = "换手率")
    @TableField(exist = false)
    private String changeRate;



    private Date createTime;

    private Date updateTime;
}
