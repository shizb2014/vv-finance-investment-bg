package com.vv.finance.investment.bg.api.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.stock.StockBrokerApi;
import com.vv.finance.investment.bg.stock.info.service.IStockBrokerComparisonService;
import com.vv.finance.investment.bg.stock.quotes.StockBrokerComparison;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName StockBrokerApiImpl
 * @Deacription 经纪席位api
 * @Author lh.sz
 * @Date 2020年11月30日 14:24
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@RequiredArgsConstructor
@Slf4j
public class StockBrokerApiImpl implements StockBrokerApi {
    @Resource
    private IStockBrokerComparisonService stockBrokerComparisonService;

    @Override
    public ResultT<StockBrokerComparison> getStockBrokerComparison(String code) {
        return ResultT.success(stockBrokerComparisonService.getOne(new QueryWrapper<StockBrokerComparison>()
                .eq("broker_code", code)));
    }

    @Override
    public ResultT<List<StockBrokerComparison>> getAllBrokerComparison() {
        return ResultT.success(stockBrokerComparisonService.list());
    }
}
