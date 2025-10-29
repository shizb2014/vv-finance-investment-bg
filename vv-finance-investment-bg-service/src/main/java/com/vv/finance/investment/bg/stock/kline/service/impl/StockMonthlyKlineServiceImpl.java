package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.vv.finance.investment.bg.stock.kline.mapper.StockMonthlyKlineMapper;
import com.vv.finance.investment.bg.stock.kline.StockMonthlyKline;
import com.vv.finance.investment.bg.stock.kline.service.IStockMonthlyKlineService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
@Service
public class StockMonthlyKlineServiceImpl extends ServiceImpl<StockMonthlyKlineMapper, StockMonthlyKline> implements IStockMonthlyKlineService {

    @Override
    public boolean batchInsert(List<StockMonthlyKline> stockMonthlyKlines) {
        return this.baseMapper.batchInsert(stockMonthlyKlines);
    }

    @Override
    public List<StockMonthlyKline> batchQuery(List<String> codes, Integer num) {
        return this.baseMapper.batchQuery(codes,num);
    }
}
