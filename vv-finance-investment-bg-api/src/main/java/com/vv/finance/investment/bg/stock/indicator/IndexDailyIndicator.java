package com.vv.finance.investment.bg.stock.indicator;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 日指标表	
 * </p>
 *
 * @author hqj
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_index_daily_indicator")
@ApiModel(value="IndexDailyIndicator对象", description="日指标表	")
public class IndexDailyIndicator extends BaseEntity {

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "毫秒时间戳")
    private Integer time;

    @ApiModelProperty(value = "5日均值")
    private Float ma5;

    @ApiModelProperty(value = "10日均值")
    private Float ma10;

    @ApiModelProperty(value = "20日均值")
    private Float ma20;

    private Float ema12;

    private Float ema26;

    private Float diff;

    private Float dea;

    private Float bar;

    private Float macd;

    @ApiModelProperty(value = "6 日相对强弱指标")
    private Float rsi6;

    @ApiModelProperty(value = "12日相对强弱指标")
    private String rsi12;

    @ApiModelProperty(value = "24日相对强弱指标")
    private String rsi24;

    @ApiModelProperty(value = "分钟数，分k返回的时候有值")
    private Integer kdjTime;

    private Float k;

    private Float d;

    private Float j;

    @ApiModelProperty(value = "boll上轨线(压力线）")
    private Float upper;

    @ApiModelProperty(value = "boll中轨线")
    private Float mid;

    @ApiModelProperty(value = "boll下轨线")
    private Float lower;




}
