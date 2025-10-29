package com.vv.finance.investment.bg.stock.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.trade.TradeStatisticsDetail;

import java.util.List;

/**
 * @author lh.sz
 */
public interface StockTradeStatisticsDetailMapper extends BaseMapper<TradeStatisticsDetail> {

    int batchSaveOrUpdate(List<TradeStatisticsDetail> tradeStatisticsDetails);

}
