package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.Stock2hourForwardKline;
import com.vv.finance.investment.bg.stock.kline.Stock2hourKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hqj
 * @since 2020-10-28
 */
public interface IStock2hourForwardKlineService   extends IService<Stock2hourForwardKline>   {
    public boolean batchInsert(List<Stock2hourForwardKline> stock2hourKlines);

    List<Stock2hourForwardKline> batchQuery(List<String> codes, Integer num);
}
