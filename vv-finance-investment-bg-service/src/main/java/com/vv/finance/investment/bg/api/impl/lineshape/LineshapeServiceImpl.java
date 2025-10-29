package com.vv.finance.investment.bg.api.impl.lineshape;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.vv.finance.common.constants.lineshape.LineShapeTraceDurationEnum;
import com.vv.finance.common.constants.lineshape.domain.StockKlineMin;
import com.vv.finance.investment.bg.api.lineshape.LineshapeService;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.mongo.model.LineShapeMQEntityNew;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntityV2;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.common.calc.hk.entity.StockKline;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: GMC
 * @Date: 2024/6/28 11:00
 * @Description: 形态数据
 * @version: 1.0
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class LineshapeServiceImpl implements LineshapeService {


    @Resource
    private MongoTemplate lineShapeTraceMongoTemplate;
    @Resource
    private HkStockRelationMapper stockRelationMapper;

    ThreadPoolExecutor threadPoolExecutor;

    @PostConstruct
    public void init() {
        TaskQueue taskQueue = new TaskQueue(12000);
        TaskThreadFactory tf = new TaskThreadFactory("delLineshapeStock_load_pool-", false, 10);
        threadPoolExecutor = new ThreadPoolExecutor(
                0,
                1,
                1L,
                TimeUnit.MINUTES,
                taskQueue,
                tf, new ThreadPoolExecutor.CallerRunsPolicy());
        taskQueue.setParent(threadPoolExecutor);
    }

    /**
     * 删除临时股票形态数据
     *
     * @param stockCode
     * @return
     */
    @Override
    public void delLineshapeByStockCode(String stockCode) {
        try {
            CompletableFuture.supplyAsync(() -> {
                long l = System.currentTimeMillis();
                log.info("删除临时股票数据 开始：stockCode：{}", stockCode);
                lineShapeTraceMongoTemplate.remove(Query.query(Criteria.where("stockKline.code").is(stockCode)), LineShapeMQEntityNew.class);
                log.info("删除临时股票数据 结束：stockCode：{} 耗时：{}", stockCode, System.currentTimeMillis() - l);
                return null;
            }, threadPoolExecutor);
        } catch (Exception e) {
            log.error("删除临时股票形态数据：stockCode：{} 异常", stockCode, e);
        }

    }

    /**
     * 变更形态股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void upLineshapeStockCode(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更形态股票code数据 开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
            Query query = Query.query(Criteria.where("stockKline.code").is(sourceCode));
            List<LineShapeMQEntityNew> lineshapeList = lineShapeTraceMongoTemplate.find(query, LineShapeMQEntityNew.class);

            if (CollUtil.isNotEmpty(lineshapeList)) {
                lineshapeList.forEach(lineshape -> {
                    lineshape.setUniqueIndex(lineshape.getUniqueIndex().replace(sourceCode, targetCode));
                    StockKlineMin stockKline = lineshape.getStockKline();
                    stockKline.setCode(targetCode);
                    lineshape.setStockKline(stockKline);
                });
                lineShapeTraceMongoTemplate.remove(query, LineShapeMQEntityNew.class);
                lineShapeTraceMongoTemplate.insertAll(lineshapeList);
            }
            log.info("变更形态股票code数据 结束：sourceCode：{} targetCode：{} 耗时：{}", sourceCode, targetCode, System.currentTimeMillis() - l);
        } catch (Exception e) {
            log.error("变更形态股票code：sourceCode：{} targetCode：{} 异常", sourceCode, targetCode, e);
        }

    }

    /**
     * 新增模拟股票形态数据
     *
     * @param simulateCode 模拟股票code
     */
    @Override
    public void saveSimulateLineshapeInfo(String simulateCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("新增模拟股票形态数据 开始：simulateCode：{} ", simulateCode);
            Long stockId = stockRelationMapper.selectStockIdByInnerCode(simulateCode);
            if (null!=stockId){
                //获取真实股票code
                String realCode = simulateCode.replace("-t","" );
                Query query = Query.query(Criteria.where("stockKline.code").is(realCode)).with(Sort.by(Sort.Order.desc("calcTimeStamp"))).limit(20);
                List<LineShapeMQEntityNew> lineshapeList = lineShapeTraceMongoTemplate.find(query, LineShapeMQEntityNew.class);
                if (CollUtil.isEmpty(lineshapeList)) {
                    log.error("新增模拟股票形态数据 失败：realCode：{} 查不到数据", realCode);
                    return;
                }
                lineshapeList.forEach(lineshape->{
                    String uniqueIndex = lineshape.getUniqueIndex();
                    StockKlineMin stockKline = lineshape.getStockKline();
                    stockKline.setStockId(stockId);
                    stockKline.setCode(simulateCode);
                    uniqueIndex = uniqueIndex.replace(realCode,simulateCode);
                    lineshape.setUniqueIndex(uniqueIndex);
                    lineshape.setStockKline(stockKline);

                });
                lineShapeTraceMongoTemplate.insertAll(lineshapeList);
                log.info("新增模拟股票形态数据 结束：simulateCode：{} 耗时：{}", simulateCode,  System.currentTimeMillis() - l);
            }else {
                log.error("新增模拟股票形态数据 失败：simulateCode：{} 查不到stockId", simulateCode);
            }

        } catch (Exception e) {
            log.error("新增模拟股票形态数据：simulateCode：{} 异常", simulateCode, e);
        }
    }

}
