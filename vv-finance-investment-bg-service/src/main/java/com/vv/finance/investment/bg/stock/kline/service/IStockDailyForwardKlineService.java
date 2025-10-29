package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.StockDailyForwardKline;
import com.vv.finance.investment.bg.stock.kline.StockDailyKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
public interface IStockDailyForwardKlineService extends IService<StockDailyForwardKline> {

    boolean batchInsert(List<StockKline> stockKlineList);

    List<StockDailyForwardKline> batchQuery(List<String> codes, Integer num);

}
