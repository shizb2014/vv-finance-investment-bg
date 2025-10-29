package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 股票月指
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_monthly_indicator")
@ApiModel(value="StockMonthlyIndicator对象", description="股票月指")
public class StockMonthlyIndicator extends BaseStockIndicator {

    private static final long serialVersionUID = 1L;




}
