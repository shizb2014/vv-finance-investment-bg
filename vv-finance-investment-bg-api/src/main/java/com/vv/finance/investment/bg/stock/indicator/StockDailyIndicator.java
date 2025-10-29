package com.vv.finance.investment.bg.stock.indicator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 股票日指
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_daily_indicator")
@ApiModel(value="StockDailyIndicator对象", description="股票日指")
public class StockDailyIndicator  extends BaseStockIndicator implements Serializable {


    private static final long serialVersionUID = -1065468561668202500L;
}
