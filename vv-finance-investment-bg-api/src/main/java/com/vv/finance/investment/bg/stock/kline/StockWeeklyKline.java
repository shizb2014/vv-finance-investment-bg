package com.vv.finance.investment.bg.stock.kline;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_weekly_kline")
@ApiModel(value="StockWeeklyKline对象", description="")
public class StockWeeklyKline extends StockKline {
    private static final long serialVersionUID = -6309818842984607460L;



}
