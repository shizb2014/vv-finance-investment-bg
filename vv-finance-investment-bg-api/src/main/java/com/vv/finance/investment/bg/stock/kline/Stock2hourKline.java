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
@TableName("t_stock_2hour_kline")
@ApiModel(value="Stock2hourKline对象", description="")
public class Stock2hourKline  extends StockKline {

    private static final long serialVersionUID = 1L;




}
