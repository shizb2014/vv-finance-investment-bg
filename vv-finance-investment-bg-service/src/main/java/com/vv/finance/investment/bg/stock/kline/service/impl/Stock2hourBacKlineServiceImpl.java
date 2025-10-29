package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.Stock2hourBackwardKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.mapper.Stock2hourBackwardKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStock2hourBaKlineService;
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
public class Stock2hourBacKlineServiceImpl extends ServiceImpl<Stock2hourBackwardKlineMapper, Stock2hourBackwardKline> implements IStock2hourBaKlineService {

    @Override
    public boolean batchInsert(List<Stock2hourBackwardKline> stock2minKlines) {
        return this.baseMapper.batchInsert(stock2minKlines) >0;
    }

    @Override
    public List<Stock2hourBackwardKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }
}
