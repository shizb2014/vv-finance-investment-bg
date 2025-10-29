package com.vv.finance.investment.bg.stock.indicator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.indicator.entity.StockYearlyIndicator;

import java.util.Collection;

/**
 * <p>
 * 年指 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface StockYearlyIndicatorMapper extends BaseMapper<StockYearlyIndicator> {
    int insertOrUpdateBatch(Collection<StockYearlyIndicator> coll);

}
