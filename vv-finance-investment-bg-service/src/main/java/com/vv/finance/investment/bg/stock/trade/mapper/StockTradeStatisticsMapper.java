package com.vv.finance.investment.bg.stock.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.move.StockMove;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;
import com.vv.finance.investment.bg.stock.info.StockDefine;

import java.util.List;

/**
 * @author lh.sz
 */
public interface StockTradeStatisticsMapper extends BaseMapper<TradeStatistics> {

    int batchSaveOrUpdate(List<TradeStatistics> tradeStatistics);
}
