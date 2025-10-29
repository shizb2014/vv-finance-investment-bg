package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.StockDailyBackwardKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.mapper.StockDailyBackwardKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStockDailyBackwardKlineService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
@Service
public class StockDailyBackwardKlineServiceImpl extends ServiceImpl<StockDailyBackwardKlineMapper, StockDailyBackwardKline> implements IStockDailyBackwardKlineService {

    @Override
    public boolean batchInsert(List<StockKline> forwardDailyKlines) {
        return this.baseMapper.batchInsert(forwardDailyKlines)>0;
    }

    @Override
    public List<StockDailyBackwardKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }
}
