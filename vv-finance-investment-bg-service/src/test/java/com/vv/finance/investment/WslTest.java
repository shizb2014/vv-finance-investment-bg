package com.vv.finance.investment;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.domain.filter.EnumValues;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.f10.ChangeBeforeAfterDividend;
import com.vv.finance.investment.bg.handler.uts.f10.DividendHandler;
//import com.vv.finance.investment.bg.job.uts.F10IndustryJob;
import com.vv.finance.investment.bg.mapper.information.NewsHKMapper;
import com.vv.finance.investment.bg.mapper.stock.quotes.CompanyTrandsMergeMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0503Mapper;
import com.vv.finance.investment.bg.stock.information.service.StockInformationServiceImpl;
import com.vv.finance.investment.bg.stock.kline.service.IStockDailyKlineService;
import com.vv.finance.investment.bg.stock.select.StockFilterRelatedApiImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 14:35
 **/
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class WslTest {

    @Resource
    private CompanyTrandsMergeMapper companyTrandsMergeMapper;

    @Resource
    private Xnhks0503Mapper xnhks0503Mapper;
    @Resource
    private NewsHKMapper newsHKMapper;
    @Resource
    private StockInformationServiceImpl service;

    @Resource
    IStockDailyKlineService stockDailyKlineService;
    @Resource
    TrendsService trendsService;
//    @Resource
//    F10IndustryJob f10IndustryJob;
    @Resource
    private MongoTemplate mongoTemplate;

//    @Autowired
//    private StockFilterRelatedApiImpl stockFilterRelatedService;

    @Autowired
    private RedisClient redisClient;

//    @Test
//    public void testService() {
//        ResultT<List<EnumValues>> listResultT = stockFilterRelatedService.enumsPlate();
//        System.out.println(listResultT);
//    }

//    @Test
//    public void enumsConcept() {
//        ResultT<List<EnumValues>> listResultT = stockFilterRelatedService.enumsConcept();
//        System.out.println(listResultT);
//    }


    @Autowired
    DividendHandler dividendHandler;
    @Test
    public void testService2() {
//        ResultT<List<ChangeBeforeAfterDividend>> listResultT = dividendHandler.changeBeforeAfterDividend("00932.hk");
//        System.out.println(listResultT);
    }

}
