package com.vv.finance.investment.bg.stock.indicator.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.indicator.entity.Stock2hourIndicator;
import com.vv.finance.investment.bg.stock.indicator.mapper.Stock2hourIndicatorMapper;
import com.vv.finance.investment.bg.stock.indicator.service.IStock2hourIndicatorService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 股票日指 服务实现类
 * </p>
 *
 * @author hqj
 * @since 2020-10-28
 */
@Service
public class Stock2hourIndicatorServiceImpl extends ServiceImpl<Stock2hourIndicatorMapper, Stock2hourIndicator> implements IStock2hourIndicatorService {


    @Override
    public boolean insertOrUpdateBatch(Collection<Stock2hourIndicator> values) {

        return this.baseMapper.insertOrUpdateBatch( values)>0;
    }
}
