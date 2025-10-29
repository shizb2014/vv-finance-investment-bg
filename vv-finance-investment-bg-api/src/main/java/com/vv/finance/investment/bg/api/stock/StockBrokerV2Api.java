package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.stock.quotes.StockBroker;

import java.util.List;

/**
 * 经纪席位api
 *
 * @author lh.sz
 */
public interface StockBrokerV2Api {

    /**
     * 获取所有的经济席位对照
     *
     * @return
     */
    ResultT<List<StockBroker>> getAllBroker();
}
