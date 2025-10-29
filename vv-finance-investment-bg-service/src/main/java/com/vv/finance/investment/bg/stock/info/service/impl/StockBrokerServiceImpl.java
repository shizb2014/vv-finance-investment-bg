package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.info.mapper.StockBrokerMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockBrokerService;
import com.vv.finance.investment.bg.stock.quotes.StockBroker;
import org.springframework.stereotype.Service;

/**
 * @ClassName StockBrokerComparisonServiceImpl
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2020年11月13日 16:10
 **/
@Service
@DS("db1")
public class StockBrokerServiceImpl extends ServiceImpl<StockBrokerMapper, StockBroker> implements IStockBrokerService {
}
