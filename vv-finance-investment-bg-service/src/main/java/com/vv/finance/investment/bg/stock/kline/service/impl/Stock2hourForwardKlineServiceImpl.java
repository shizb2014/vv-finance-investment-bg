package com.vv.finance.investment.bg.stock.kline.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.kline.Stock2hourForwardKline;
import com.vv.finance.investment.bg.stock.kline.mapper.Stock2hourForwardKlineMapper;
import com.vv.finance.investment.bg.stock.kline.service.IStock2hourForwardKlineService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hqj
 * @since 2020-10-28
 */
@Service
public  class Stock2hourForwardKlineServiceImpl extends ServiceImpl<Stock2hourForwardKlineMapper,Stock2hourForwardKline> implements IStock2hourForwardKlineService {

    @Override
    public boolean batchInsert(List<Stock2hourForwardKline> stock2hourKlines) {
        return this.baseMapper.batchInsert(stock2hourKlines) >0;
    }

    @Override
    public List<Stock2hourForwardKline> batchQuery(List<String> codes, Integer num) {
        return null;
    }


}
