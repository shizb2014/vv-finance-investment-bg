package com.vv.finance.investment.bg.stock.kline.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.Stock2hourBackwardKline;
import com.vv.finance.investment.bg.stock.kline.Stock2hourForwardKline;
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
public interface IStock2hourBaKlineService extends IService<Stock2hourBackwardKline> {

    boolean batchInsert(List<Stock2hourBackwardKline> stock1hourKlines);

    List<Stock2hourBackwardKline> batchQuery(List<String> codes, Integer num);
}
