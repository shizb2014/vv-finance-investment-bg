package com.vv.finance.investment.bg.stock.indicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.indicator.StockDailyIndicator;
import com.vv.finance.investment.bg.stock.indicator.entity.StockWeeklyIndicator;
import com.vv.finance.investment.bg.stock.indicator.mapper.StockWeeklyIndicatorMapper;
import com.vv.finance.investment.bg.stock.indicator.service.IStockWeeklyIndicatorService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 股票周指 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Service
public class StockWeeklyIndicatorServiceImpl extends ServiceImpl<StockWeeklyIndicatorMapper, StockWeeklyIndicator> implements IStockWeeklyIndicatorService {

    @Override
    public boolean insertOrUpdateBatch(Collection<StockWeeklyIndicator> values) {
        return this.baseMapper.insertOrUpdateBatch(values)>0;
    }
}
