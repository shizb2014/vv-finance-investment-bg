package com.vv.finance.investment.bg.stock.indicator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.indicator.entity.Stock2hourIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票日指 Mapper 接口
 * </p>
 *
 * @author hqj
 * @since 2020-10-28
 */
public interface Stock2hourIndicatorMapper extends BaseMapper<Stock2hourIndicator> {

    int insertOrUpdateBatch(Collection<Stock2hourIndicator> coll);
}
