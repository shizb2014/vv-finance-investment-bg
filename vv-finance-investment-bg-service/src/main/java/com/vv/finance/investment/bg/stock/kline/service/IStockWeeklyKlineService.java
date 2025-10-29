package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.StockMonthlyKline;
import com.vv.finance.investment.bg.stock.kline.StockWeeklyKline;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface IStockWeeklyKlineService extends IService<StockWeeklyKline> {

    boolean batchInsert(List<StockWeeklyKline> list);

    List<StockWeeklyKline> batchQuery(List<String> codes, Integer num);
}
