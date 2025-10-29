package com.vv.finance.investment.bg.stock.indicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.indicator.StockDailyIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票日指 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
public interface IStockDailyIndicatorService extends IService<StockDailyIndicator> {

    boolean insertOrUpdateBatch(Collection<StockDailyIndicator> values);
}
