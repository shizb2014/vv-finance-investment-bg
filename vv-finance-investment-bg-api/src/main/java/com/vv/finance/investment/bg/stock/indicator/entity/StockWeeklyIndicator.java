package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 股票周指
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_weekly_indicator")
@ApiModel(value="StockWeeklyIndicator对象", description="股票周指")
public class StockWeeklyIndicator extends BaseStockIndicator {

    private static final long serialVersionUID = 1L;




}
