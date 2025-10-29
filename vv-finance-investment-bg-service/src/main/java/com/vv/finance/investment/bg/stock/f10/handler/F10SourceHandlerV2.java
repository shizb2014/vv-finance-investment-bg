package com.vv.finance.investment.bg.stock.f10.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.F10TableTemplateV2;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.mongo.dao.F10AssetsLiabilitiesDao;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.dao.F10ProfitDao;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import com.vv.finance.investment.bg.mongo.model.F10EntityBase;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.TableSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class F10SourceHandlerV2 {
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

    @Resource
    RedisClient redisClient;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    public List<List<F10TableTemplateV2>> builderF10Source(F10PageReq f10PageReq, int tableSource, List<F10TableTemplateV2> list) {
        List<List<F10TableTemplateV2>> list1 = new ArrayList<>();
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

    public <T extends F10EntityBase> F10PageResp<T> f10PageResp(F10PageReq f10PageReq, int tableSource) {
        List<List<F10TableTemplateV2>> list1 = new ArrayList<>();
        TableSourceTypeEnum typeEnum = TableSourceTypeEnum.getByCode(tableSource);
        F10PageResp<T> f10PageResp = new F10PageResp<>();
        switch (Objects.requireNonNull(typeEnum)) {
            case INDEX_NO_FINANCE:
                f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
                break;
            case INDEX_FINANCE:
                f10PageResp = f10KeyFiguresDao.pageFinancial(f10PageReq);
                break;
            case INDEX_INSURANCE:
                f10PageResp = f10KeyFiguresDao.pageInsurance(f10PageReq);
                break;
            case PROFIT_NO_FINANCE:
                f10PageResp = f10ProfitDao.pageNonFinancial(f10PageReq);
                break;
            case PROFIT_FINANCE:
                f10PageResp = f10ProfitDao.pageFinancial(f10PageReq);
                break;
            case PROFIT_INSURANCE:
                f10PageResp = f10ProfitDao.pageInsurance(f10PageReq);
                break;
            case ASSET_NO_FINANCE:
                f10PageResp = f10AssetsLiabilitiesDao.pageNonFinancial(f10PageReq);
                break;
            case ASSET_FINANCE:
                f10PageResp = f10AssetsLiabilitiesDao.pageFinancial(f10PageReq);
                break;
            case ASSET_INSURANCE:
                f10PageResp = f10AssetsLiabilitiesDao.pageInsurance(f10PageReq);
                break;
            case CASH_FLOW:
                f10PageResp = f10CashFlowDao.pageCashFlow(f10PageReq);
                break;

            default:
                break;
        }

        return f10PageResp;
    }

    /**
     * 主要指标非金融
     *
     * @param f10PageReq
     * @param list
     * @return
     */
    private List<List<F10TableTemplateV2>> builderF10ByNoFinIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplateV2> list
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
    private List<List<F10TableTemplateV2>> builderF10ByFinIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplateV2> list
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
    private List<List<F10TableTemplateV2>> builderF10ByInsuranceIndex(
            F10PageReq f10PageReq,
            List<F10TableTemplateV2> list
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
    private List<List<F10TableTemplateV2>> builderF10Cash(
            F10PageReq f10PageReq,
            List<F10TableTemplateV2> list
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
    private List<List<F10TableTemplateV2>> profitNoFinance(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    private List<List<F10TableTemplateV2>> profitFinance(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    private List<List<F10TableTemplateV2>> profitInsurance(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    private List<List<F10TableTemplateV2>> assetsNoFin(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    private List<List<F10TableTemplateV2>> assetsFin(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    private List<List<F10TableTemplateV2>> assetsInsurance(F10PageReq f10PageReq, List<F10TableTemplateV2> list) {
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
    public  <T> List<List<F10TableTemplateV2>> buildTables(List<F10TableTemplateV2> list, List<T> source) {
        if (CollUtil.isEmpty(source)) {
            return Lists.newArrayList();
        }
        List<List<F10TableTemplateV2>> lists = source.stream().map(item -> {
            List<F10TableTemplateV2> templates = new ArrayList<>();
            for (F10TableTemplateV2 f10TableTemplate : list) {
                F10TableTemplateV2 tableTemplate = ObjectUtil.cloneByStream(f10TableTemplate);
                templates.add(tableTemplate);
            }
            setVal(templates, item);
            return templates;
        }).collect(Collectors.toList());

        return lists;
    }

    /**
     * 设值
     *
     * @param templates
     * @param oriSource
     * @param <T>
     */
    private <T> void setVal(List<F10TableTemplateV2> templates, T oriSource) {
        if (oriSource == null) {
            return;
        }
        Field[] fields = ReflectUtil.getFields(oriSource.getClass());
        Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity(), (v1, v2) -> v2));

        Map<String, Object> fieldValueMap = getFieldValueMap(oriSource);

        Set<String> templatesFiled = templates.stream().map(F10TableTemplateV2::getMappedFields).collect(Collectors.toSet());
        Map<String, F10TableTemplateV2> mapTemplate = Maps.newConcurrentMap();
        for (F10TableTemplateV2 template : templates) {
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
            } else {
                if (fieldValueMap.containsKey(mappedFields) && StrUtil.isBlank(item.getFieldValue())) {
                    Object fieldValue = fieldValueMap.get(mappedFields);
                    if (ObjectUtil.isNotEmpty(fieldValue)) {
                        item.setFieldValue(JSON.toJSONString(fieldValue, SerializerFeature.WriteMapNullValue));
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
                if (StrUtil.isBlank(StrUtil.unWrap(template.getFieldValue(), CharPool.DOUBLE_QUOTES))) {
                    template.setFieldValue(null);
                }
            }
        });

    }

    private <T> Map<String, Object> getFieldValueMap(T oriSource) {
        Map<String, Object> objectMap = new HashMap<>();
        Field[] fields = ReflectUtil.getFields(oriSource.getClass());
        Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity(), (v1, v2) -> v2));
        fieldMap.forEach((fn, fm) -> {
            Class<?> superclass = fm.getType().getSuperclass();
            Object oriVal = ReflectUtil.getFieldValue(oriSource, fm);
            if (F10Val.class == superclass) {
                Field[] subFields = ReflectUtil.getFields(fm.getType());
                for (Field subField : subFields) {
                    objectMap.put(subField.getName(), ReflectUtil.getFieldValue(oriVal, subField));
                }
            } else {
                objectMap.put(fn, oriVal);
            }
        });
        return objectMap;
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

    public List<LocalDate> getThirtyTradingDays(String stockCode, boolean containsToday) {
        LocalDate localDate = containsToday ? LocalDate.now() : LocalDate.now().plusDays(-1);
        // 1. 正常股票显示近30个交易日
        // 2. 停牌股票显示停牌日近30个交易日
        // StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockCode));
        // if (ObjectUtil.isNotNull(snapshot) && StockStatueEnum.STOP.getCode() == snapshot.getSuspension()) {
        //     // 停牌股票显示停牌日近30个交易日
        //     StockKlineReq stockKlineReq = new StockKlineReq();
        //     stockKlineReq.setCode(stockCode);
        //     stockKlineReq.setType(OmdcCommonConstant.DAY);
        //     stockKlineReq.setTime(System.currentTimeMillis());
        //     stockKlineReq.setAdjhkt(Adjhkt.FORWARD);
        //     stockKlineReq.setNum(1);
        //     List<KlineEntity> klineEntities = hkStockCompositeApi.selectKlineList(stockKlineReq);
        //     if (CollUtil.isNotEmpty(klineEntities)) {
        //         DateUtil.date(klineEntities.get(0).getTime());
        //         localDate = LocalDateTimeUtil.of(klineEntities.get(0).getTime()).toLocalDate();
        //     }
        // }

        ResultT<List<BgTradingCalendar>> listResultT = hkTradingCalendarApi.getLastTradingCalendars(localDate, 30);
        return listResultT.getData().stream().sorted(Comparator.comparing(BgTradingCalendar::getDate)).map(BgTradingCalendar::getDate).collect(Collectors.toList());
    }
}
