package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.StockDailyKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.mapper.StockDailyKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStockDailyKlineService;
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
public class StockDailyKlineServiceImpl extends ServiceImpl<StockDailyKlineMapper, StockDailyKline> implements IStockDailyKlineService {

    @Override
    public boolean batchInsert(List<StockKline> list) {
        return this.baseMapper.batchInsert(list)>0;
    }

    @Override
    public List<StockDailyKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }
}
