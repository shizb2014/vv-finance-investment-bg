package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.kline.StockWeeklyKline;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface StockWeeklyKlineMapper extends BaseMapper<StockWeeklyKline> {

    int batchInsert(List<StockWeeklyKline> list);
}
