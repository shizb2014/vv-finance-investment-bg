package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 年指
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_yearly_indicator")
@ApiModel(value="StockYearlyIndicator对象", description="年指")
public class StockYearlyIndicator extends BaseStockIndicator {

    private static final long serialVersionUID = 1L;




}
