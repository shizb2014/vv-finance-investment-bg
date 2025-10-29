package com.vv.finance.investment.bg.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * @author hamilton
 * @date 2020/11/21 16:28
 */
@Data
@EqualsAndHashCode
public class BgTradingTime implements Serializable {

    private static final long serialVersionUID = -2109419458117310579L;
    @ApiModelProperty(value = "开始时间")
    private LocalTime tradingStartTime;
    @ApiModelProperty(value = "结束时间")
    private LocalTime tradingEndTime;


}
