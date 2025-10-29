package com.vv.finance.investment.bg.stock.indicator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@TableName("t_stock_2hour_indicator")
@ApiModel(value="Stock2hourIndicator对象", description="2小时指标表	")
public class Stock2hourIndicator extends BaseStockIndicator {
    private static final long serialVersionUID = 1L;


}
