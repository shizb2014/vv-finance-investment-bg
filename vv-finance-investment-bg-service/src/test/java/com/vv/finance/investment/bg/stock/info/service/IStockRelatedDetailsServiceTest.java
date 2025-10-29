package com.vv.finance.investment.bg.stock.info.service;

import com.google.common.collect.Lists;
import com.vv.finance.investment.bg.BGServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * something
 *
 * @author Edison
 * @version 1.0 2021/12/30 16:00
 *
 * <p></p>
 */
@SpringBootTest(classes = BGServiceApplication.class)
class IStockRelatedDetailsServiceTest {

    @Autowired
    private IStockRelatedDetailsService stockRelatedDetailsService;

//    @Test
//    void repairStocksDetail() {
//        stockRelatedDetailsService.repairStocksDetailSnapshot(Lists.newArrayList("00700.hk"));
//    }
}