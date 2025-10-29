package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.entity.StockTrade;

import java.util.List;

/**
 * <p>
 * 委托挂单 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface StockTradeMapper extends BaseMapper<StockTrade> {

    int batchSaveOrUpdate(List<StockTrade> stockTrades);
}
