package com.vv.finance.investment.bg.dto.kline;

import com.vv.finance.investment.bg.dto.info.DealDTO;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/11/12 9:51
 */
@Data
public class SimpleKlineDto implements Serializable {
    private static final long serialVersionUID =1;
//    @ApiModelProperty("股票代码")
//    private String code;

    @ApiModelProperty("时间戳")
    private Long time;

    @ApiModelProperty("开盘价")
    private BigDecimal open;
    @ApiModelProperty("最高价")
    private BigDecimal high;
    @ApiModelProperty("最低价")
    private BigDecimal low;
    @ApiModelProperty("收盘价")
    private BigDecimal close;

    /**
     * 在分k 和 日周年月中的定义是否一致
     */
    @ApiModelProperty("昨日收盘价")
    private BigDecimal preClose;

    @ApiModelProperty("成交量")
    private BigDecimal volume;
    @ApiModelProperty("涨跌额")
    private BigDecimal chg;
    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;
    @ApiModelProperty("成交额")
    private BigDecimal amount;
    @ApiModelProperty(value = "平均价")
    private BigDecimal vwap;
    @ApiModelProperty(value = "换手率")
    private BigDecimal changeRate;

    /**
     * @see com.vv.finance.common.constants.kline.ExitRightKeyConstants
     */
    @ApiModelProperty(value = "事件字段")
    private List<EventDTO> event;

    @ApiModelProperty(value = "买卖点")
    private List<DealDTO> dealList;



}
