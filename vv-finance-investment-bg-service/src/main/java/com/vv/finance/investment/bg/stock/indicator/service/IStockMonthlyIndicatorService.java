package com.vv.finance.investment.bg.stock.indicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.indicator.entity.StockMonthlyIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票月指 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface IStockMonthlyIndicatorService extends IService<StockMonthlyIndicator> {
    boolean insertOrUpdateBatch(Collection<StockMonthlyIndicator> values);

}
