package com.vv.finance.investment.bg.stock.indicator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.indicator.entity.StockMonthlyIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票月指 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface StockMonthlyIndicatorMapper extends BaseMapper<StockMonthlyIndicator> {
    int insertOrUpdateBatch(Collection<StockMonthlyIndicator> coll);
}
