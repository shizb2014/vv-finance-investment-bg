package com.vv.finance.investment.bg.api.impl.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.stock.StockBrokerV2Api;
import com.vv.finance.investment.bg.stock.info.service.IStockBrokerService;
import com.vv.finance.investment.bg.stock.quotes.StockBroker;
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
public class StockBrokerApiV2Impl implements StockBrokerV2Api {
    @Resource
    private IStockBrokerService stockBrokerService;

    @Override
    public ResultT<List<StockBroker>> getAllBroker() {
        return ResultT.success(stockBrokerService.list());
    }
}
