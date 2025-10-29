package com.vv.finance.investment.bg.stock.indicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.indicator.StockDailyIndicator;
import com.vv.finance.investment.bg.stock.indicator.mapper.StockDailyIndicatorMapper;
import com.vv.finance.investment.bg.stock.indicator.service.IStockDailyIndicatorService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 股票日指 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-27
 */
@Service
public class StockDailyIndicatorServiceImpl extends ServiceImpl<StockDailyIndicatorMapper, StockDailyIndicator> implements IStockDailyIndicatorService {

    @Override
    public boolean insertOrUpdateBatch(Collection<StockDailyIndicator> values) {
        return this.baseMapper.insertOrUpdateBatch( values)>0;
    }
}
