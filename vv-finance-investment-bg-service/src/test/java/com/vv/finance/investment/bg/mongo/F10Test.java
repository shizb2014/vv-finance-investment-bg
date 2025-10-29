package com.vv.finance.investment.bg.mongo;

import cn.hutool.core.collection.CollUtil;
import com.vv.finance.investment.bg.entity.f10.EstimationSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.GrowthContrastSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.ScaleSortEnum;
import com.vv.finance.investment.bg.entity.f10.industry.GrowthContrast;
import com.vv.finance.investment.bg.handler.uts.sync.*;
//import com.vv.finance.investment.bg.job.uts.F10IndustryJob;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10EstimationAnalyzeServiceImpl;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10IndustryContrastServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/20 10:19
 */
@SpringBootTest
public class F10Test {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    F10KeyFiguresInsuranceHandler f10KeyFiguresInsuranceHandler;
    @Resource
    F10KeyFiguresNonFinancialHandler f10KeyFiguresNonFinancialHandler;
    @Resource
    F10KeyFiguresFinancialHandler f10KeyFiguresFinancialHandler;
    @Resource
    F10NoFinProfitHandler f10NoFinProfitHandler;
    @Resource
    F10FinProfitHandler f10FinProfitHandler;
    @Resource
    F10AssetsLiabilitiesNonFinancialHandler f10AssetsLiabilitiesNonFinancialHandler;
    @Resource
    F10AssetsLiabilitiesFinancialHandler f10AssetsLiabilitiesFinancialHandler;
    @Resource
    F10InsureProfitHandler f10InsureProfitHandler;
    @Resource
    F10CashFlowHandler f10CashFlowHandler;
    @Resource
    F10AssetsLiabilitiesInsuranceHandler f10AssetsLiabilitiesInsuranceHandler;
    @Resource
    F10IndustryContrastServiceImpl f10IndustryContrastService;
    @Resource
    F10EstimationAnalyzeServiceImpl f10EstimationAnalyzeService;
//    @Resource
//    F10IndustryJob f10IndustryJob;

    @Test
    public void f10FinProfitHandlerTest() {
        f10KeyFiguresFinancialHandler.sync();
    }

    @Test
    public void F10KeyFiguresNonFinancialHandlerTest(){
        f10KeyFiguresNonFinancialHandler.sync();
    }

    @Test
    public void F10KeyFiguresNonFinancialHandlerTest1(){
        Aggregation minAggregation = Aggregation.newAggregation(Aggregation.group("stockCode").first("stockCode").as("stockCode"));
        AggregationResults<F10NoFinProfitEntity> aggregate = mongoTemplate.aggregate(minAggregation, "f10_profit_no_financial", F10NoFinProfitEntity.class);
        List<F10NoFinProfitEntity> f10KeyFiguresNonFinancialEntities = aggregate.getMappedResults();
        if (CollUtil.isNotEmpty(f10KeyFiguresNonFinancialEntities)) {
            Set<String> stockCodes = f10KeyFiguresNonFinancialEntities.stream().map(entity -> entity.getStockCode()).collect(Collectors.toSet());

            if (CollUtil.isNotEmpty(f10KeyFiguresNonFinancialEntities)) {
                stockCodes.forEach(stockCode->{
                    f10KeyFiguresNonFinancialHandler.doSync(stockCode,null);
                });
            }

        }
    }

    @Test
    public void F10KeyFiguresNonFinancialHandlerTest2(){
        f10KeyFiguresNonFinancialHandler.doSync("00981.hk",null);
    }


    @Test
    public void checkIndustry() {
//        List<GrowthContrast> growthContrast = f10IndustryContrastService.getGrowthContrast("08299.hk", GrowthContrastSortEnum.EARNINGS_PER_SHARE);
        List<GrowthContrast> growthContrast1 = f10IndustryContrastService.getGrowthContrast("00340.hk", GrowthContrastSortEnum.EARNINGS_PER_SHARE);
        System.out.println(growthContrast1);
    }

    @Test
    public void checkGetEstimation() {
//        f10IndustryContrastService.getEstimation("00700.hk", EstimationSortEnum.PB);
    }

    @Test
    public void checkGetScale() {
        f10IndustryContrastService.getScale("00005.hk", ScaleSortEnum.GROSS_MARGIN);
    }

    @Test
    public void checkEs() {
        f10EstimationAnalyzeService.getEstimationAnalyzeChar("00700.hk");
    }

    @Test
    public void checkRadarChar() {
        f10EstimationAnalyzeService.getEstimationRadarChar("00700.hk");
    }

//    @Test
//    public void checkIndustryJob() {
//        f10IndustryJob.saveMarketIndustry(new Date());
//    }
//
//    @Test
//    public void checkIndexJob() {
//        f10IndustryJob.saveMarketIndex(new Date());
//    }

    @Test
    public void checkMarket() {
//        f10IndustryContrastService.getMarketPresence("00700.hk");
    }
}
