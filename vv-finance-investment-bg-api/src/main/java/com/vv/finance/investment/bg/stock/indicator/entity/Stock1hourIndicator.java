package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 2小时指标表	
 * </p>
 *
 * @author hqj
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_1hour_indicator")
@ApiModel(value="Stock1hourIndicator对象", description="1小时指标表	")
public class Stock1hourIndicator extends BaseStockIndicator {
    private static final long serialVersionUID = 1L;


}
