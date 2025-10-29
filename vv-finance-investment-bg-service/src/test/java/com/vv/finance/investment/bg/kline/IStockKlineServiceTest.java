package com.vv.finance.investment.bg.kline;

import com.vv.finance.common.route.RouteKlineReq;
import com.vv.finance.common.route.RouteKlineResp;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/11/23 16:34
 */
@SpringBootTest
public class IStockKlineServiceTest {

    @Autowired
    StockInfoApi stockInfoApi;

    @Autowired
    StockService stockService;
    @Test
    public void testAggKline(){
        List<StockQueryDTO> stockQueryDTOS = stockService.queryStockSort("0001");
        System.out.println("aa");
    }

    @Test
    public void initStockDefine(){
        //码表
        stockInfoApi.initStockDefine();
    }
}
