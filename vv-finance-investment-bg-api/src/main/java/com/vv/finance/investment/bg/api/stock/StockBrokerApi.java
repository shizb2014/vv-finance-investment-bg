package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.stock.quotes.StockBrokerComparison;

import java.util.List;

/**
 * 经纪席位api
 *
 * @author lh.sz
 */
public interface StockBrokerApi {
    /**
     * 获取经纪席位对照
     *
     * @param code 经纪席位code
     * @return
     */
    ResultT<StockBrokerComparison> getStockBrokerComparison(String code);

    /**
     * 获取所有的经济席位对照
     *
     * @return
     */
    ResultT<List<StockBrokerComparison>> getAllBrokerComparison();
}
