package com.vv.finance.investment.bg.stock.f10.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.F10TableTemplate;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.mongo.dao.F10AssetsLiabilitiesDao;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.dao.F10ProfitDao;
import com.vv.finance.investment.bg.mongo.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregationOptions;

/**
 * @ClassName F10SourceHandler
 * @Deacription 组装f10数据
 * @Author lh.sz
 * @Date 2021年07月25日 14:21
 **/
@Component
@Slf4j
public class F10SourceHandler {
    @Resource
    F10KeyFiguresDao f10KeyFiguresDao;
    @Resource
    F10ProfitDao f10ProfitDao;
    @Resource
    F10AssetsLiabilitiesDao f10AssetsLiabilitiesDao;
    @Resource
    F10CashFlowDao f10CashFlowDao;

    @Resource
    private MongoTemplate mongoTemplate;

    public List<List<F10TableTemplate>> builderF10Source(
            F10PageReq f10PageReq,
            int tableSource,
            List<F10TableTemplate> list

    ) {
        List<List<F10TableTemplate>> list1 = new ArrayList<>();
        TableSourceTypeEnum typeEnum = TableSourceTypeEnum.getByCode(tableSource);
        switch (Objects.requireNonNull(typeEnum)) {
            case INDEX_NO_FINANCE:
                return builderF10ByNoFinIndex(f10PageReq, list);
            case INDEX_FINANCE:
                return builderF10ByFinIndex(f10PageReq, list);
            case INDEX_INSURANCE:
                return builderF10ByInsuranceIndex(f10PageReq, list);
            case PROFIT_NO_FINANCE:
                return profitNoFinance(f10PageReq, list);
            case PROFIT_FINANCE:
                return profitFinance(f10PageReq, list);
            case PROFIT_INSURANCE:
                return profitInsurance(f10PageReq, list);
            case ASSET_NO_FINANCE:
                return assetsNoFin(f10PageReq, list);
            case ASSET_FINANCE:
                return assetsFin(f10PageReq, list);
            case ASSET_INSURANCE:
                return assetsInsurance(f10PageReq, list);
            case CASH_FLOW:
                return builderF10Cash(f10PageReq, list);

            default:
                break;
        }
        return list1;
    }

    /**
     * 主要指标非金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> builderF10ByNoFinIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplate> list
    ) {
        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 主要指标金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> builderF10ByFinIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplate> list
    ) {
        F10PageResp<F10KeyFiguresFinancialEntity> f10PageResp = f10KeyFiguresDao.pageFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 主要指标保险
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> builderF10ByInsuranceIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplate> list
    ) {
        F10PageResp<F10KeyFiguresInsuranceEntity> f10PageResp = f10KeyFiguresDao.pageInsurance(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }


    /**
     * 现金流量表
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> builderF10Cash(
            F10PageReq f10PageReq,
            List<F10TableTemplate> list
    ) {

        F10PageResp<F10CashFlowEntity> f10PageResp = f10CashFlowDao.pageCashFlow(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 利润非金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> profitNoFinance(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10ProfitDao.pageNonFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 利润金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> profitFinance(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10ProfitDao.pageFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 利润保险
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> profitInsurance(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10ProfitDao.pageInsurance(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 资产负债非金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> assetsNoFin(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10AssetsLiabilitiesDao.pageNonFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 资产负债金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> assetsFin(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10AssetsLiabilitiesDao.pageFinancial(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 资产负债保险
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplate>> assetsInsurance(F10PageReq f10PageReq, List<F10TableTemplate> list) {
        F10PageResp f10PageResp = f10AssetsLiabilitiesDao.pageInsurance(f10PageReq);
        return buildTables(list, f10PageResp.getRecord());
    }

    /**
     * 组装table
     *
     * @param list
     * @param source
     * @param <T>
     * @return
     */
    private <T> List<List<F10TableTemplate>> buildTables(List<F10TableTemplate> list, List<T> source) {
        if (CollUtil.isEmpty(source)) {
            return Lists.newArrayList();
        }
        return source.stream().map(item -> {
            List<F10TableTemplate> templates = new ArrayList<>();
            for (F10TableTemplate f10TableTemplate : list) {
                F10TableTemplate tableTemplate = ObjectUtil.cloneByStream(f10TableTemplate);
                templates.add(tableTemplate);
            }
            setVal(templates, item);
            return templates;
        }).collect(Collectors.toList());
    }

    /**
     * 设值
     *
     * @param templates
     * @param oriSource
     * @param <T>
     */
    private <T> void setVal(List<F10TableTemplate> templates, T oriSource) {
        if (oriSource == null) {
            return;
        }
        Field[] fields = ReflectUtil.getFields(oriSource.getClass());
        Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity(), (v1, v2) -> v2));
        Set<String> templatesFiled = templates.stream().map(F10TableTemplate::getMappedFields).collect(Collectors.toSet());
        Map<String, F10TableTemplate> mapTemplate = Maps.newConcurrentMap();
        for (F10TableTemplate template : templates) {
            mapTemplate.put(template.getMappedFields(), template);
        }
        templates.forEach(item -> {
            String mappedFields = item.getMappedFields();
            Field field = fieldMap.get(mappedFields);
            if (field != null) {
                Object fieldValue = ReflectUtil.getFieldValue(oriSource, field);
                if (fieldValue != null && item.getIsNull() == 1) {
                    item.setFieldValue(JSON.toJSONString(fieldValue, SerializerFeature.WriteMapNullValue));
                    if (item.getIsOverstriking() == 0) {
                        Field[] valFields = ReflectUtil.getFields(fieldValue.getClass());
                        for (Field valField : valFields) {
                            if (templatesFiled.contains(valField.getName())) {
                                Object fieldValue1 = ReflectUtil.getFieldValue(fieldValue, valField);
                                if (fieldValue1 != null) {
                                    ReflectUtil.setFieldValue(mapTemplate.get(valField.getName()), "fieldValue", JSON.toJSONString(fieldValue1, SerializerFeature.WriteMapNullValue));
                                }
                            }

                        }
                    }
                } else if (fieldValue != null && item.getIsNull() == 0) {
                    Field[] valFields = ReflectUtil.getFields(fieldValue.getClass());
                    for (Field valField : valFields) {
                        if (templatesFiled.contains(valField.getName())) {
                            Object fieldValue1 = ReflectUtil.getFieldValue(fieldValue, valField);
                            if (fieldValue1 != null) {
                                ReflectUtil.setFieldValue(mapTemplate.get(valField.getName()), "fieldValue", JSON.toJSONString(fieldValue1, SerializerFeature.WriteMapNullValue));
                            }
                        }

                    }
                }

            }

        });
        templates.forEach(template -> {
            if (StringUtils.isBlank(template.getFieldValue()) && !"auditOpinion".equals(template.getMappedFields())) {
                template.setFieldValue(JSON.toJSONString(new F10Val(), SerializerFeature.WriteMapNullValue));
            }
            if ("auditOpinion".equals(template.getMappedFields()) && StringUtils.isNotBlank(template.getFieldValue())) {
                if (template.getFieldValue().contains("1")) {
                    template.setFieldValue("无保留意见");
                }
                if (template.getFieldValue().contains("2")) {
                    template.setFieldValue("无保留意见#");
                }
                if (template.getFieldValue().contains("3")) {
                    template.setFieldValue("有保留意见");
                }

            }
        });

    }

    /**
     * 获取财报数据
     *
     * @param clazz
     * @param collectionName
     * @param <T>
     * @return
     */
    public <T> List<T> listFinanceEachCodeAndType(Class<T> clazz, String collectionName, String[] reportTypes) {
        GroupOperation group = Aggregation.group("stockCode", "reportType");
        Field[] fields = clazz.getDeclaredFields();
        Field[] declaredFields = clazz.getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            if (!"serialVersionUID".equals(field.getName())) {
                group = group.first(field.getName()).as(field.getName());
            }
        }
        for (Field field : declaredFields) {
            if (!"serialVersionUID".equals(field.getName())) {
                group = group.first(field.getName()).as(field.getName());
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("reportType").in(reportTypes)), Aggregation.sort(Sort.Direction.DESC, "endTimestamp"), group)
                .withOptions(newAggregationOptions().allowDiskUse(true).build());
        return mongoTemplate.aggregate(aggregation, collectionName, clazz).getMappedResults();
    }


    public <T> List<T> allFinanceEachCodeAndType(Collection<String> codes, Class<T> clazz, String collectionName, List<String> reportTypes) {
        Criteria criteria = Criteria.where("reportType").in(reportTypes);
        if (CollUtil.isNotEmpty(codes)) {
            criteria = criteria.andOperator(Criteria.where("stockCode").in(codes));
        }
        Query query = new Query(criteria);
        return mongoTemplate.find(query, clazz, collectionName);
    }
}
