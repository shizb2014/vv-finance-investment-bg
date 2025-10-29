package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.kline.StockDailyBackwardKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
public interface StockDailyBackwardKlineMapper extends BaseMapper<StockDailyBackwardKline> {

    int batchInsert(List<StockKline> list);
}
