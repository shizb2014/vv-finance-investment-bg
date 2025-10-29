package com.vv.finance.investment.bg.api.impl.lineshape;

import cn.hutool.core.collection.CollUtil;
import com.vv.finance.common.constants.lineshape.LineShapeIndicatorEnum;
import com.vv.finance.common.constants.lineshape.LineShapeStockFormEnum;
import com.vv.finance.common.constants.lineshape.LineShapeTraceDurationEnum;
import com.vv.finance.common.dto.TechnicalIndicatorDTONew;
import com.vv.finance.investment.bg.api.quotation.ITechnicalIndicatorService;
import com.vv.finance.investment.bg.mongo.model.LineShapeMQEntityNew;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author qinxi
 * @date 2024/4/18 20:29
 * @description:
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class TechnicalIndicatorServiceImpl implements ITechnicalIndicatorService {


    @Resource
    private MongoTemplate lineShapeTraceMongoTemplate;

    @Override
    public Map<String, List<TechnicalIndicatorDTONew>> getTechnicalIndicatorMap(Collection<String> codes, Integer limit, Integer duration, long localTimeStamp) {
        log.info("getTechnicalIndicatorMap start code:{} limit:{} duration:{}", codes, limit, duration);
        Map<String, List<TechnicalIndicatorDTONew>> map = new HashMap<>();
        long start = System.currentTimeMillis();
        //当日数据查询本地内存
        LineShapeTraceDurationEnum durationEnum = LineShapeTraceDurationEnum.getByCode(duration);
//        RpcContext.getContext().setAttachment(Constants.TAG_KEY, durationEnum.getEnglishName());
//        List<CompletableFuture<Void>> completableFutureList = new ArrayList<>(codes.size());
//        for (String code : codes) {
//            completableFutureList.add(CompletableFuture.runAsync(() -> {
        //比较查询出来的数据，是否足够翻页的条数，不够再去查询MongoDb
        long t2 = System.currentTimeMillis();
        ProjectionOperation projectionOperation = Aggregation.project("stockKline", "durationEnum", "indicatorEnumList","calcTimeStamp");
        Criteria criteria = Criteria.where("stockKline.code").in(codes)
                .and("durationEnum").is(LineShapeTraceDurationEnum.getByCode(duration))
                .and("calcTimeStamp").gte(localTimeStamp);
        Aggregation aggregation = Aggregation.newAggregation( // 构建聚合操作
                Aggregation.match(criteria),
                Aggregation.sort(Sort.by(Sort.Order.desc("calcTimeStamp"))),
//                            Aggregation.limit(remainingPageSize),
                projectionOperation
        );
        List<LineShapeMQEntityNew> allLineShapeList = lineShapeTraceMongoTemplate.aggregate(aggregation, "line_shape_list", LineShapeMQEntityNew.class).getMappedResults();

        log.info("allLineShapeList查询MongoDB耗时========={}ms  共{}条记录" , (System.currentTimeMillis() - t2), allLineShapeList.size());
        Map<String, List<LineShapeMQEntityNew>> collect = allLineShapeList.stream().collect(Collectors.groupingBy(entity -> entity.getStockKline().getCode()));
        collect.forEach((stockCode, lineShapeList) -> {
            List<TechnicalIndicatorDTONew> dtoList = new ArrayList<>(limit);
            lineShapeList.sort(Comparator.comparingLong(LineShapeMQEntityNew::getCalcTimeStamp).reversed());
            List<TechnicalIndicatorDTONew> historyList = lineShapeList.subList(0, lineShapeList.size() > limit ? limit : lineShapeList.size()).stream()
                    .map(item -> {
                        TechnicalIndicatorDTONew dto = new TechnicalIndicatorDTONew();
                        List<LineShapeIndicatorEnum> indicatorEnumList = item.getIndicatorEnumList();
                        dto.setCode(item.getStockKline().getCode());
                        dto.setIndicatorEnumList(indicatorEnumList);
                        dto.setPrice(item.getStockKline().getClose());
                        dto.setTime(item.getStockKline().getTime());
                        return dto;
                    })
                    .collect(Collectors.toList());
            dtoList.addAll(historyList);
            dtoList.sort(Comparator.comparingLong(item -> {
                TechnicalIndicatorDTONew i = (TechnicalIndicatorDTONew) item;
                return i.getTime();
            }).reversed());
            map.put(stockCode, dtoList);
        });
//            }, EXECUTOR_SERVICE));
//        }
        log.info("getHistoryKlineByCodes耗时========={}ms" , (System.currentTimeMillis() - t2));
        log.info("getTechnicalIndicatorMap end code:{} limit:{} duration:{} resultSize:{} 耗时:{}ms", codes, limit, duration, map.size(), System.currentTimeMillis() - start);
        return map;
    }

    @Override
    public Map<String, List<LineShapeStockFormEnum>> getLineShapeStockFormEnum(Collection<String> codes, Integer duration, long timestamp) {
        long t = System.currentTimeMillis();
        LineShapeTraceDurationEnum durationEnum = LineShapeTraceDurationEnum.getByCode(duration);
        ProjectionOperation  projectionOperation = Aggregation.project("stockKline", "stockFormEnumList");
        Criteria  criteria = Criteria.where("stockKline.code").in(codes)
                .and("durationEnum").is(durationEnum)
                .and("calcTimeStamp").is(timestamp);
        Aggregation  aggregation = Aggregation.newAggregation(
                // 构建聚合操作
                Aggregation.match(criteria),
                projectionOperation
        );
        Map<String, List<LineShapeStockFormEnum>> map = new ConcurrentHashMap<>();
        List<LineShapeMQEntityNew> allLineShapeList = lineShapeTraceMongoTemplate.aggregate(aggregation, "line_shape_list", LineShapeMQEntityNew.class).getMappedResults();
        log.info("allLineShapeList查询MongoDB耗时========={}ms timestamp：{} duration:{} 查询总数:{}  一共查出{}条记录", (System.currentTimeMillis() - t), timestamp, durationEnum, codes.size(), allLineShapeList.size());
        if (CollUtil.isNotEmpty(allLineShapeList)) {
            allLineShapeList.parallelStream().forEach(o -> {
                map.put(o.getStockKline().getCode(), o.getStockFormEnumList());
            });
        }
        log.info("map共{}条记录", map.size());
        return map;
    }

}
