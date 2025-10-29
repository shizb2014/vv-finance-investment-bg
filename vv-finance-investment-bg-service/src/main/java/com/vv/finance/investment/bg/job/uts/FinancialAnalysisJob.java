package com.vv.finance.investment.bg.job.uts;

import cn.hutool.core.collection.CollUtil;
import com.vv.finance.investment.bg.handler.stock.StockHandler;
import com.vv.finance.investment.bg.handler.uts.sync.AbstractF10CommonHandler;
import com.vv.finance.investment.bg.handler.uts.sync.F10AssetsLiabilitiesInsuranceHandler;
import com.vv.finance.investment.bg.handler.uts.sync.F10CashFlowHandler;
import com.vv.finance.investment.bg.handler.uts.sync.F10FinProfitHandler;
import com.vv.finance.investment.bg.handler.uts.sync.F10InsureProfitHandler;
import com.vv.finance.investment.bg.handler.uts.sync.F10KeyFiguresNonFinancialHandler;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import com.vv.finance.investment.bg.mongo.model.F10EntityBase;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/20 11:14
 * 财务分析数据同步
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinancialAnalysisJob {
    private final List<AbstractF10CommonHandler> f10CommonHandlerList;

    private final ExecutorService jobExecutorService;
    @Resource
    private F10KeyFiguresNonFinancialHandler f10KeyFiguresNonFinancialHandler;
    @Resource
    private F10FinProfitHandler f10FinProfitHandler;
    @Resource
    private F10InsureProfitHandler f10InsureProfitHandler;
    @Resource
    private F10CashFlowHandler f10CashFlowHandler;
    @Resource
    private F10AssetsLiabilitiesInsuranceHandler f10AssetsLiabilitiesInsuranceHandler;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Resource
    private StockHandler stockHandler;

    //同步到mongodb f10财务分析数据同步入口
    @XxlJob(value = "syncFinancialAnalysis", author = "蒋正泽", desc = "f10财务分析数据同步")
    @SneakyThrows
    public ReturnT<String> syncFinancialAnalysis(String param) {
        CountDownLatch countDownLatch = new CountDownLatch(f10CommonHandlerList.size());
        for (AbstractF10CommonHandler f10CommonHandler : f10CommonHandlerList) {
            String simpleName = f10CommonHandler.getClass().getSimpleName();
            jobExecutorService.execute(() -> {
                try {
                    log.info("syncFinancialAnalysis start, handler: {}", simpleName);
                    f10CommonHandler.sync();
                    log.info("syncFinancialAnalysis end, handler: {}", simpleName);
                } catch (Exception e) {
                    log.info("syncFinancialAnalysis error, handler: {}", simpleName, e);
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();
        return ReturnT.SUCCESS;
    }

    //同步到mongodb f10财务分析数据同步入口
    @XxlJob(value = "syncAllFinancialAnalysis", author = "杨鹏", desc = "全量更新F10财务数据")
    @SneakyThrows
    public ReturnT<String> syncAllFinancialAnalysis(String param) {
        CountDownLatch countDownLatch = new CountDownLatch(f10CommonHandlerList.size());
        for (AbstractF10CommonHandler f10CommonHandler : f10CommonHandlerList) {
            String simpleName = f10CommonHandler.getClass().getSimpleName();
            jobExecutorService.execute(() -> {
                try {
                    log.info("syncAllFinancialAnalysis start, handler: {}", simpleName);
                    f10CommonHandler.syncAll();
                    log.info("syncAllFinancialAnalysis end, handler: {}", simpleName);
                } catch (Exception e) {
                    log.info("syncAllFinancialAnalysis error, handler: {}", simpleName, e);
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();
        return ReturnT.SUCCESS;
    }

    /**
     * 初始化非金融财务分析
     */
    @XxlJob(value = "initNotFinancialAnalysis", author = "龚敏川", desc = "初始化非金融财务分析")
    public ReturnT<String> initNotFinancialAnalysis(String param) {
        log.info("==============初始化非金融财务分析===执行开始=========");
        long beginTime = System.currentTimeMillis();
        Aggregation minAggregation = Aggregation.newAggregation(Aggregation.group("stockCode").first("stockCode").as("stockCode"));
        AggregationResults<F10KeyFiguresNonFinancialEntity> aggregate = mongoTemplate.aggregate(minAggregation, "f10_key_figures_non_financial", F10KeyFiguresNonFinancialEntity.class);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntities = aggregate.getMappedResults();
        if (CollUtil.isNotEmpty(f10KeyFiguresNonFinancialEntities)) {
            Set<String> stockCodes = f10KeyFiguresNonFinancialEntities.stream().map(entity -> entity.getStockCode()).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(f10KeyFiguresNonFinancialEntities)) {
                stockCodes.forEach(stockCode->{
                f10KeyFiguresNonFinancialHandler.doSync(stockCode,null);
                });
            }

        }
        long endTime = System.currentTimeMillis();
        log.info("==============初始化非金融财务分析===执行结束======耗时 :{}===",endTime-beginTime);
        return ReturnT.SUCCESS;
    }

    /**
     * 初始化利润表财务分析
     */
    @XxlJob(value = "initProfitAnalysis", author = "杨鹏", desc = "初始化利润表财务分析")
    public ReturnT<String> initProfitAnalysis(String param) {
        log.info("==============初始化利润表财务分析===执行开始=========");
        long beginTime = System.currentTimeMillis();

        syncF10Data("f10_profit_financial", f10FinProfitHandler, F10FinProfitEntity.class);
        syncF10Data("f10_profit_insure", f10InsureProfitHandler, F10InsureProfitEntity.class);
        syncF10Data("f10_cash_flow", f10CashFlowHandler, F10CashFlowEntity.class);
        syncF10Data("f10_assets_liabilities_insurance", f10AssetsLiabilitiesInsuranceHandler, F10AssetsLiabilitiesInsuranceEntity.class);

        long endTime = System.currentTimeMillis();
        log.info("==============初始化利润表财务分析===执行结束======耗时 :{}===", endTime - beginTime);
        return ReturnT.SUCCESS;
    }

    private <T extends F10EntityBase, V extends AbstractF10CommonHandler> void syncF10Data(String collectionName, V handler, Class<T> clazz) {
        Aggregation minAggregation = Aggregation.newAggregation(Aggregation.group("stockCode").first("stockCode").as("stockCode"));
        AggregationResults<T> aggregate = mongoTemplate.aggregate(minAggregation, collectionName, clazz);
        List<T> mappedResults = aggregate.getMappedResults();
        if (CollUtil.isNotEmpty(mappedResults)) {
            Set<String> stockCodes = mappedResults.stream().map(F10EntityBase::getStockCode).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(mappedResults)) {
                stockCodes.forEach(stockCode -> {
                    handler.doSync(stockCode, null);
                });
            }
        }
    }

    @XxlJob(value = "saveRankShortSale", author = "杨鹏", desc = "保存沽空排行榜单", cron = "0 50 6 * * ?")
    public ReturnT<String> saveRankShortSale(String param) {
        log.info("==============保存沽空排行榜单===执行开始=========");
        long beginTime = System.currentTimeMillis();
        stockHandler.saveDailyRankShortSale();
        long endTime = System.currentTimeMillis();
        log.info("==============保存沽空排行榜单===执行结束======耗时 :{}===", endTime - beginTime);
        return ReturnT.SUCCESS;
    }
}
