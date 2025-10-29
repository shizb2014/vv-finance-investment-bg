package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.quotes.StockBroker;

import java.util.Collection;

/**
 * @author lh.sz
 */
public interface StockBrokerMapper extends BaseMapper<StockBroker> {

    int insertOrUpdateBatch(Collection<StockBroker> coll);

}
