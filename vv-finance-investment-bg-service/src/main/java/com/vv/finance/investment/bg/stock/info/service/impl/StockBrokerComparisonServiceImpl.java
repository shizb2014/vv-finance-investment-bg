package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.info.mapper.StockBrokerComparisonMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockBrokerComparisonService;
import com.vv.finance.investment.bg.stock.quotes.StockBrokerComparison;
import org.springframework.stereotype.Service;

/**
 * @ClassName StockBrokerComparisonServiceImpl
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2020年11月13日 16:10
 **/
@Service
public class StockBrokerComparisonServiceImpl extends ServiceImpl<StockBrokerComparisonMapper, StockBrokerComparison> implements IStockBrokerComparisonService  {
}
