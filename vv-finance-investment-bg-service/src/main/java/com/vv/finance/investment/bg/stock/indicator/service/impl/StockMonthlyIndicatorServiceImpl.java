package com.vv.finance.investment.bg.stock.indicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.indicator.entity.StockMonthlyIndicator;
import com.vv.finance.investment.bg.stock.indicator.mapper.StockMonthlyIndicatorMapper;
import com.vv.finance.investment.bg.stock.indicator.service.IStockMonthlyIndicatorService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 股票月指 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Service
public class StockMonthlyIndicatorServiceImpl extends ServiceImpl<StockMonthlyIndicatorMapper, StockMonthlyIndicator> implements IStockMonthlyIndicatorService {

    @Override
    public boolean insertOrUpdateBatch(Collection<StockMonthlyIndicator> values) {
        return this.baseMapper.insertOrUpdateBatch(values)>0;
    }
}
