package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.Stock2hourKline;
import com.vv.finance.investment.bg.stock.kline.StockDailyBackwardKline;
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
public interface IStock2hourKlineService extends IService<Stock2hourKline> {
    boolean batchInsert(List<Stock2hourKline> stock2hourKlines);

    List<Stock2hourKline> batchQuery(List<String> codes, Integer num);

}
