package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.StockDailyBackwardKline;
import com.vv.finance.investment.bg.stock.kline.StockDailyForwardKline;
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
public interface IStockDailyBackwardKlineService extends IService<StockDailyBackwardKline> {

    boolean batchInsert(List<StockKline> forwardDailyKlines);

    List<StockDailyBackwardKline> batchQuery(List<String> codes, Integer num);
}
