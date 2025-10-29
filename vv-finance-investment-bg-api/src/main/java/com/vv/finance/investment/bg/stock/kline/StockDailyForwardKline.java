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
 * @since 2020-10-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_daily_forward_kline")
@ApiModel(value="StockDailyForwardKline对象", description="")
public class StockDailyForwardKline  extends StockKline {


    private static final long serialVersionUID = -7875803072107132892L;
}
