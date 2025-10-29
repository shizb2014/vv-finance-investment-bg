package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.info.entity.StockTrade;
import com.vv.finance.investment.bg.stock.info.mapper.StockTradeMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockTradeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 逐笔交易 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Service
public class StockTradeServiceImpl extends ServiceImpl<StockTradeMapper, StockTrade> implements IStockTradeService {

    @Override
    public boolean batchSaveOrUpdate(List<StockTrade> stockTrades) {

        return  this.baseMapper.batchSaveOrUpdate(stockTrades) >0;
    }
}
