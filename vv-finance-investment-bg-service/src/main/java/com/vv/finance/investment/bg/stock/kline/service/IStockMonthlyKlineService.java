package com.vv.finance.investment.bg.stock.kline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.kline.StockMonthlyKline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface IStockMonthlyKlineService extends IService<StockMonthlyKline> {

    boolean batchInsert(List<StockMonthlyKline> stockMonthlyKlines);

    List<StockMonthlyKline> batchQuery( List<String> codes, Integer num);
}
