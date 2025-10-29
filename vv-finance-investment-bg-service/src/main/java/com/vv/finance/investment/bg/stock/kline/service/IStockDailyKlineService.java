package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.StockDailyKline;
import com.vv.finance.investment.bg.stock.kline.StockYearlyKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface IStockDailyKlineService extends IService<StockDailyKline> {

    boolean batchInsert(List<StockKline> list);

    List<StockDailyKline> batchQuery(List<String> codes, Integer num);


}
