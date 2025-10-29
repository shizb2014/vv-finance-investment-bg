package com.vv.finance.investment.broker;

import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.broker.BrokerHKStockHotSpotApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.impl.HkTradingCalendarApiImpl;
import com.vv.finance.investment.bg.dto.broker.BrokersPositionStatisticsDetail;
import com.vv.finance.investment.bg.dto.broker.NetBuyAndSellResp;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 经纪商需求测试类
 * @Auther: shizhibiao
 * @Date: 2022/10/21
 * @Description: com.vv.finance.investment.broker
 * @version: 1.0
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class MsBrokerAnalysisTest {

    @Autowired
    BrokerAnalysisApi brokerAnalysisApi;

    @Autowired
    BrokerHKStockHotSpotApi brokerHKStockHotSpotApi;

    /**
     * 个股主页-十大净买入卖出排行
     */
    @Test
    public void getNetTradeBroker(){

//        ResultT<NetBuyAndSellResp> netTradeBroker = brokerAnalysisApi.getNetTradeBroker("00700.hk", 0);
//        log.info(netTradeBroker.toString());

//        ResultT<NetBuyAndSellResp> netTradeBroker1 = brokerAnalysisApi.getNetTradeBroker("00700.hk", 1);
//        log.info(netTradeBroker1.toString());

//        ResultT<List<BrokersPositionStatisticsDetail>> netTradeBroker2 = brokerAnalysisApi.getBrokerPositionStatistics("00700.hk");


//        ResultT<NetBuyAndSellResp> netTradeBroker2 = brokerAnalysisApi.getNetTradeBroker("00700.hk", 2);
//        log.info(netTradeBroker2.toString());
//
//        ResultT<NetBuyAndSellResp> netTradeBroker3 = brokerAnalysisApi.getNetTradeBroker("00700.hk", 3);
//        log.info(netTradeBroker3.toString());
//
//        ResultT<NetBuyAndSellResp> netTradeBroker4 = brokerAnalysisApi.getNetTradeBroker("00700.hk", 4);
//        log.info(netTradeBroker4.toString());

    }

    /**
     * 个股主页-十大净买入卖出排行
     */
    @Test
    public void getBrokerHoldingsRank(){

        brokerAnalysisApi.updateRedisBrokerJob("02991.hk", LocalDate.now());
    }





}
