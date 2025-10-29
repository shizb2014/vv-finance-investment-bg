package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author hamilton
 * @date 2020/10/28 14:49
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseStockIndicator  implements Serializable {
    private static final long serialVersionUID = 7898775295886375398L;
    @ApiModelProperty(value = "主键id")
    @TableId(value = "id",type = IdType.NONE)
    private Long id;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "毫秒时间戳")
    private Long date;
    @ApiModelProperty(value = "字符串时间")
    @TableField(exist = false)
    private Date dateNotes;
    @TableField(value = "ema12")
    private BigDecimal ema12;
    @TableField(value = "ema26")
    private BigDecimal ema26;

    private BigDecimal diff;

    private BigDecimal dea;

    private BigDecimal macd;

    @ApiModelProperty(value = "boll上轨线(压力线）")
    @TableField(value = "boll_upper")
    private BigDecimal bollUpper;

    @ApiModelProperty(value = "boll中轨线")
    private BigDecimal mid;

    @ApiModelProperty(value = "boll下轨线")
    @TableField(value = "boll_lower")
    private BigDecimal bollLower;


    @ApiModelProperty(value = "RSI6指标")
    private BigDecimal rsi6UpAverage;

    @ApiModelProperty(value = "RSI6指标")
    private BigDecimal rsi6DownAverage;

    @ApiModelProperty(value = "RSI12指标")
    private BigDecimal rsi12UpAverage;

    @ApiModelProperty(value = "RSI12指标")
    private BigDecimal rsi12DownAverage;



    @ApiModelProperty(value = "6 日相对强弱指标")
    @TableField(value = "rsi6")
    private BigDecimal rsi6;

    @ApiModelProperty(value = "12日相对强弱指标")
    @TableField(value = "rsi12")
    private BigDecimal rsi12;

    private BigDecimal k;

    private BigDecimal d;

    private BigDecimal j;



    @ApiModelProperty(value = "上升动向均值")
    private BigDecimal dmUpAvg;

    @ApiModelProperty(value = "下跌动向")
    private BigDecimal dmDown;

    @ApiModelProperty(value = "上升动向")
    private BigDecimal dmUp;

    @ApiModelProperty(value = "下跌动向敦治")
    private BigDecimal dmDownAvg;

    @ApiModelProperty(value = "真实波幅")
    private BigDecimal tr;

    @ApiModelProperty(value = "真实波幅均值")
    private BigDecimal trAvg;

    @ApiModelProperty(value = "上升方向指标")
    private BigDecimal pdi;

    @ApiModelProperty(value = "下跌方向指标")
    private BigDecimal pdm;

    @ApiModelProperty(value = "动向值")
    private BigDecimal dx;

    @ApiModelProperty(value = "平均趋向指数")
    private BigDecimal adx;

    private BigDecimal adxr;

    private BigDecimal wr;


    private BigDecimal obv;

    private BigDecimal sar;

    private BigDecimal sarFacto;

    private BigDecimal sarUp;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField( exist = false,value = "ema5")
    private BigDecimal ema5;
    @TableField(exist = false,value = "ema10")
    private BigDecimal ema10;
    @TableField(exist = false,value = "ema20")
    private BigDecimal ema20;
    @TableField(exist = false,value = "ema30")
    private BigDecimal ema30;
    @TableField(exist = false,value = "ema50")
    private BigDecimal ema50;
    @TableField(exist = false,value = "ema60")
    private BigDecimal ema60;
    @TableField(exist = false,value = "ema120")
    private BigDecimal ema120;

    @ApiModelProperty(value = "24日相对强弱指标")
    @TableField(exist = false,value = "rsi24")
    private BigDecimal rsi24;
    @ApiModelProperty(value = "RSI24指标")
    @TableField(exist = false,value = "rsi24")
    private BigDecimal rsi24UpAverage;

    @ApiModelProperty(value = "RSI指标")
    @TableField(exist = false,value = "rsi24")
    private BigDecimal rsi24DownAverage;

    @ApiModelProperty(value = "数据来源")
    @TableField(exist = false,value = "rsi24")
    private Integer source;





}
