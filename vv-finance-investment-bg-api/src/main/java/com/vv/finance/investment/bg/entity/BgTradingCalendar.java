package com.vv.finance.investment.bg.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="TradingCalendar对象", description="")
public class BgTradingCalendar implements Serializable {


    private static final long serialVersionUID = 5676754371506354673L;
    @ApiModelProperty(value = "yyyyMMdd 公历日期")
    private LocalDate date;

//
//    @TableField(value = "trading_time",typeHandler = FastjsonTypeHandler.class)
//    @ApiModelProperty(value = "交易时段")
//    private List<BgTradingTime> tradingTimeList;


    @ApiModelProperty(value = "0 不是交易日 1交易日 2 上午交易 3 下午交易")
    /**
     * @see com.vv.finance.investment.bg.constants.TradingCalendarState
     */
    private Integer tradingDay;







}
