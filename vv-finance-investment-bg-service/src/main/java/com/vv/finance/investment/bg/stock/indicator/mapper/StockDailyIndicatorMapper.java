package com.vv.finance.investment.bg.stock.indicator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.indicator.StockDailyIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票日指 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
public interface StockDailyIndicatorMapper extends BaseMapper<StockDailyIndicator> {

    int insertOrUpdateBatch(Collection<StockDailyIndicator> coll);
}
