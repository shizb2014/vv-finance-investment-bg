package com.vv.finance.investment.bg.stock.kline.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author chenyu
 * @date 2020/11/3 17:06
 */
@Data
public class BaseStockKlineEntity implements Serializable {

    private static final long serialVersionUID = 524544996429993791L;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "时间")
    private Long time;
    @ApiModelProperty(value = "时间")
    @TableField(exist = false)
    private Date date;
    @ApiModelProperty(value = "成交量")
    private BigDecimal volume;

    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "成交额")
    private BigDecimal amount;
}
