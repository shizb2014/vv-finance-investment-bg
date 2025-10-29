package com.vv.finance.investment.bg.stock.indicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.indicator.entity.StockYearlyIndicator;

import java.util.Collection;

/**
 * <p>
 * 年指 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface IStockYearlyIndicatorService extends IService<StockYearlyIndicator> {
    boolean insertOrUpdateBatch(Collection<StockYearlyIndicator> values);

}
