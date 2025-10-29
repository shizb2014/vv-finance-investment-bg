package com.vv.finance.investment.bg.stock.indicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.indicator.entity.StockWeeklyIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票周指 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface IStockWeeklyIndicatorService extends IService<StockWeeklyIndicator> {
    boolean insertOrUpdateBatch(Collection<StockWeeklyIndicator> values);

}
