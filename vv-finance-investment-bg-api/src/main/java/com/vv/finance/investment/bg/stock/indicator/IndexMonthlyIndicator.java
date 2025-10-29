package com.vv.finance.investment.bg.stock.indicator;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 月指标表	
 * </p>
 *
 * @author hqj
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_index_monthly_indicator")
@ApiModel(value="IndexMonthlyIndicator对象", description="月指标表	")
public class IndexMonthlyIndicator extends BaseEntity {


    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "日期")
    private Date date;

    @TableField("upAverage")
    private BigDecimal upAverage;

    @TableField("downAverage")
    private BigDecimal downAverage;

    private BigDecimal ema12;

    private BigDecimal ema26;

    private BigDecimal diff;

    private BigDecimal dea;

    private BigDecimal bar;

    private BigDecimal macd;

    @ApiModelProperty(value = "6 日相对强弱指标")
    private BigDecimal rsi6;

    @ApiModelProperty(value = "12日相对强弱指标")
    private BigDecimal rsi12;

    @ApiModelProperty(value = "24日相对强弱指标")
    private BigDecimal rsi24;

    @ApiModelProperty(value = "分钟数，分k返回的时候有值")
    private Integer kdjTime;

    private BigDecimal k;

    private BigDecimal d;

    private BigDecimal j;

    @ApiModelProperty(value = "boll上轨线(压力线）")
    private BigDecimal upper;

    @ApiModelProperty(value = "boll中轨线")
    private BigDecimal mid;

    @ApiModelProperty(value = "boll下轨线")
    private BigDecimal lower;

//    private Float dmUp;
//
//    private Float dmUpAvg;
//
//    private Float dmDown;
//
//    private Float dmDownAvg;
//
//    private Float tr;
//
//    private Float trAvg;
//
//    private Float pdi;
//
//    private Float pdm;
//
//    private Float dx;
//
//    private Float adx;
//
//    private Float adxr;


}
