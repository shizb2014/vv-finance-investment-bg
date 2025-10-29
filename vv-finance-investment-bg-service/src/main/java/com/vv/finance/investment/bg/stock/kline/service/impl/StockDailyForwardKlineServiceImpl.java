package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.StockDailyForwardKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.mapper.StockDailyForwardKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStockDailyForwardKlineService;
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
public class StockDailyForwardKlineServiceImpl extends ServiceImpl<StockDailyForwardKlineMapper, StockDailyForwardKline> implements IStockDailyForwardKlineService {

    @Override
    public boolean batchInsert(List<StockKline> stockKlineList){
        return this.baseMapper.batchInsert(stockKlineList)>0;
    }

    @Override
    public List<StockDailyForwardKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }

}
