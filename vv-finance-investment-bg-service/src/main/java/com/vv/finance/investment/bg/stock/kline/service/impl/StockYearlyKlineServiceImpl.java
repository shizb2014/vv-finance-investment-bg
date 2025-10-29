//package com.vv.finance.investment.bg.stock.kline.service.impl;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.vv.finance.investment.bg.stock.kline.service.IStockYearlyKlineService;
//import com.vv.finance.investment.bg.stock.kline.StockYearlyKline;
//import com.vv.finance.investment.bg.stock.kline.mapper.StockYearlyKlineMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * <p>
// *  服务实现类
// * </p>
// *
// * @author hamilton
// * @since 2020-10-26
// */
//@Service
//public class StockYearlyKlineServiceImpl extends ServiceImpl<StockYearlyKlineMapper, StockYearlyKline> implements IStockYearlyKlineService {
//
//    @Override
//    public boolean batchInsert(List<StockYearlyKline> list){
//        return this.baseMapper.batchInsert(list)>0;
//    }
//
//    @Override
//    public List<StockYearlyKline> batchQuery(List<String> codes, Integer num) {
//        return null;
//    }
//}
