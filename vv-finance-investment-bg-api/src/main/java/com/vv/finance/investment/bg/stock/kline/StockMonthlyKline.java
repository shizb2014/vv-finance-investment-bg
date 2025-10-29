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
@TableName("t_stock_monthly_kline")
@ApiModel(value="StockMonthlyKline对象", description="")
public class StockMonthlyKline extends StockKline {


    private static final long serialVersionUID = -1049483808678843612L;
}
