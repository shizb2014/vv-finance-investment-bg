package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.kline.StockYearlyKline;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface StockYearlyKlineMapper extends BaseMapper<StockYearlyKline> {

    int batchInsert(List<StockYearlyKline> list);

}
