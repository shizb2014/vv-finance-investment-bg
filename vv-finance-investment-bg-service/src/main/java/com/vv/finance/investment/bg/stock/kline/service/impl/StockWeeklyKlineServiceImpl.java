package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.service.IStockWeeklyKlineService;
import com.vv.finance.investment.bg.stock.kline.StockWeeklyKline;
import com.vv.finance.investment.bg.stock.kline.mapper.StockWeeklyKlineMapper;
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
public class StockWeeklyKlineServiceImpl extends ServiceImpl<StockWeeklyKlineMapper, StockWeeklyKline> implements IStockWeeklyKlineService {

    @Override
    public boolean batchInsert(List<StockWeeklyKline> list) {
        return this.baseMapper.batchInsert(list)>0;
    }

    @Override
    public List<StockWeeklyKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }
}
