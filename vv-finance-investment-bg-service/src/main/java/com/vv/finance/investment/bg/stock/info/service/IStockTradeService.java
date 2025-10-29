package com.vv.finance.investment.bg.stock.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.info.entity.StockTrade;

import java.util.List;

/**
 * <p>
 * 委托挂单 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface IStockTradeService extends IService<StockTrade> {

    boolean batchSaveOrUpdate(List<StockTrade> stockTrades);
}
