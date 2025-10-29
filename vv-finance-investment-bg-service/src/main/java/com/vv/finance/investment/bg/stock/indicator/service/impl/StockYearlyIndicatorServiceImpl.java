package com.vv.finance.investment.bg.stock.indicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.indicator.entity.StockYearlyIndicator;
import com.vv.finance.investment.bg.stock.indicator.mapper.StockYearlyIndicatorMapper;
import com.vv.finance.investment.bg.stock.indicator.service.IStockYearlyIndicatorService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 年指 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Service
public class StockYearlyIndicatorServiceImpl extends ServiceImpl<StockYearlyIndicatorMapper, StockYearlyIndicator> implements IStockYearlyIndicatorService {

    @Override
    public boolean insertOrUpdateBatch(Collection<StockYearlyIndicator> values) {
        return this.baseMapper.insertOrUpdateBatch(values)>0;
    }
}
