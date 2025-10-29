package com.vv.finance.investment.bg.stock.kline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.mapper.Stock2hourKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStock2hourKlineService;
import com.vv.finance.investment.bg.stock.kline.Stock2hourKline;
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
public class Stock2hourKlineServiceImpl extends ServiceImpl<Stock2hourKlineMapper, Stock2hourKline> implements IStock2hourKlineService {

    @Override
    public boolean batchInsert(List<Stock2hourKline> stock2hourKlines) {
        return this.baseMapper.batchInsert(stock2hourKlines) >0;
    }

    @Override
    public List<Stock2hourKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }
}
