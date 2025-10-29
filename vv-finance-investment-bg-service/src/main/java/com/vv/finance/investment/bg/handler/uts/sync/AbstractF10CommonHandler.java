package com.vv.finance.investment.bg.handler.uts.sync;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.vv.finance.common.enums.ComReportTypeEnum;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10EntityBase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/22 17:04
 */
@Slf4j
public abstract class AbstractF10CommonHandler {
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 同步数据
     */
    public abstract void sync();

    /**
     * 同步数据
     */
    public abstract void syncAll();

    /**
     * 数据同步校验
     * @param code
     * @param modifiedDate
     * @param tClass
     * @param <T>
     */
    public  <T extends F10EntityBase>void syncCheck(String code,Date modifiedDate,Class<T> tClass){
        try {
            T v = mongoTemplate.
                    findOne(new Query(Criteria.where("stockCode").is(code)).
                            with(Sort.by(Sort.Direction.DESC, "updateTime")).limit(1), tClass);

            if (v != null && modifiedDate != null) {
                Date updateTime = v.getUpdateTime();
                if (updateTime == null || updateTime.before(modifiedDate)) {
                    doSync(code, updateTime);
                    log.info("更新数据 tClass={},code={},modifiedDate={},updateTime={}", tClass.getSimpleName(), code, modifiedDate, updateTime);
                }
            } else {
                mongoTemplate.remove(Query.query(Criteria.where("stockCode").is(code)), tClass);
                doSync(code, null);
            }
        }catch (Exception e){
            log.error("syncCheck exception tClass={}",tClass,e);
        }

    }

    protected <E> List<E> convertAndFilterReports(List<E> inputList, Function<E, String> typeFun, BiConsumer<E, String> typeConsumer, Function<E, Number> numFun) {
        // 1. 判断Q3和Q9类型
        // 2. 过滤掉不满足条件的类型
        if (CollUtil.isEmpty(inputList)) {
            return inputList;
        }

        List<E> resultList = inputList.stream().map(e -> {
            String type = typeFun.apply(e);
            Number number = numFun.apply(e);
            if (ObjectUtil.isEmpty(type) || ObjectUtil.isEmpty(number)) {
                return null;
            }
            BigDecimal quarter = BigDecimal.valueOf(number.doubleValue());
            if (StrUtil.equals(ComReportTypeEnum.Q3.getCode(), type)) {
                String reportType = type;
                if (NumberUtil.equals(BigDecimal.valueOf(3), quarter)) {
                    // Q3类型
                    reportType = ComReportTypeEnum.Q3.getCode();
                } else if (NumberUtil.equals(BigDecimal.valueOf(9), quarter)) {
                    // Q9类型
                    reportType = ComReportTypeEnum.Q9.getCode();
                } else {
                    Object code = ReflectUtil.getFieldValue(e, "seccode");
                    log.info("AbstractF10CommonHandler convertAndFilterReports filter report, code|reportType|number: {}|{}|{}", code, type, number);
                    // 不需要的类型，过滤掉
                    return null;
                }
                // 更新类型
                typeConsumer.accept(e, reportType);
            }
            return e;
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());

        log.info("AbstractF10CommonHandler convertAndFilterReports input|result: {}|{}", CollUtil.size(inputList), CollUtil.size(resultList));

        return inputList;
    }

    /**
     * 执行数据同步
     * @param code
     * @param update
     */
    public abstract void doSync(String code, Date update);

    /**
     * @param val1 公布日期
     * @param val2 截至日期
     * @param val3 报告类型
     * @return
     */
    public String buildKey(Long val1, Long val2, String val3) {
        return val1 + "-" + val2 + "-" + val3;

    }

    /**
     * 计算同比
     *
     * @param newVal  最新值
     * @param lastVal 上一笔的值
     * @return
     */
    public BigDecimal calYoy(BigDecimal newVal, BigDecimal lastVal) {
        return BigDecimalUtil.calYoy(newVal, lastVal);
    }

    /**
     * 计算净利率
     *
     * @param val  净利润
     * @param val2 营业收入
     * @return
     */
    public BigDecimal calcNetProfitRatio(BigDecimal val, BigDecimal val2) {
        if (val == null || val2 == null) {
            return null;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return null;
        }

        return val.divide(val2, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    /**
     * @param val  经营活动现金流
     * @param val2 总股本
     * @return
     */
    public BigDecimal calcCash(BigDecimal val, BigDecimal val2) {
        if (val == null || val2 == null) {
            return null;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return null;
        }
        return val.divide(val2, 2, RoundingMode.HALF_UP);
    }

    /**
     * 要百分化的数据保留到4位
     * @param val
     * @param val2
     * @return
     */
    public BigDecimal calcCashFour(BigDecimal val, BigDecimal val2) {
        if (val == null || val2 == null) {
            return null;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return null;
        }
        return val.divide(val2, 4, RoundingMode.HALF_UP);
    }

    public String calStartDate(String date, Long month) {
        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
        LocalDate plusMonths = localDate.minusMonths(month - 1).withDayOfMonth(1);
        return plusMonths.format(dateTimeFormatter);
    }

    public String dateFormat(Long date) {
        String dateStr = date.toString();
        return dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6);
    }

    @SneakyThrows
    public long dateStrToLong(String date) {
        return DateUtils.parseDate(date, "yyyy/MM/dd").getTime();
    }


    public <T extends F10EntityBase> void save(T v, Class<T> tClass) {
        Query query = Query.query(Criteria.where("stockCode").is(v.getStockCode())
                .and("reportType").is(v.getReportType())
                .and("endDate").is(v.getEndDate())
                .and("startDate").is(v.getStartDate()));
        T entity = mongoTemplate.findOne(query, tClass);
        if (entity != null) {
            mongoTemplate.remove(query, tClass);
        }
        replaceNull(entity, v);
        mongoTemplate.save(v);
    }

    public <T extends F10EntityBase> void saveBulk(List<T> lists, Class<T> tClass) {
        if (CollectionUtils.isEmpty(lists)) {
            return;
        }
        List<T> multiList = new ArrayList<>();
        lists.forEach(t -> {
            //通过股票code、财报类型、财报日期确认财报的唯一性，多次发布的同类型财报以最后一次为准
            Query query = Query.query(
                    Criteria.where("stockCode").is(t.getStockCode())
                            .and("reportType").is(t.getReportType())
                            .and("endDate").is(t.getEndDate()));

            List<T> entitys = mongoTemplate.find(query, tClass);
            T lastEntity=null;
            if (CollUtil.isNotEmpty(entitys)) {
                mongoTemplate.remove(query, tClass);
                entitys.sort(Comparator.comparing(o->o.getEndTimestamp()));
                lastEntity = entitys.get(entitys.size()-1);
            }
            replaceNull(lastEntity, t);
            multiList.add(t);
        });

        mongoTemplate.insert(multiList,tClass);
    }

    @SneakyThrows
    public <T> void replaceNull(T source, T target) {

        Field[] fields = ReflectUtil.getFields(target.getClass());
        for (Field field : fields) {
            if ("serialVersionUID".equals(field.getName()) || "updateTime".equals(field.getName())) {
                continue;
            }
            Object o = ReflectUtil.getFieldValue(target, field);
            if (o == null) {
                if (source != null) {
                    o = ReflectUtil.getFieldValue(source, field);
                }
                if (o == null) {
                    o = ReflectUtil.newInstance(field.getType());
                }
                ReflectUtil.setFieldValue(target, field, o);

            }
        }
    }

    public BigDecimal multiply(BigDecimal left,BigDecimal right){
        if(left==null || right == null){
            return null;
        }
        return left.multiply(right);
    }

    /**
     * left.divide(right)
     * @param left
     * @param right
     * @return
     */
    public BigDecimal divideRate(BigDecimal left,BigDecimal right){
        if(left==null || right == null){
            return null;
        }
        return left.divide(right,5,RoundingMode.HALF_UP);
    }

    /**
     * 百分化
     * @date 2022/8/26 17:17
     * @param: val
     * @return: java.math.BigDecimal
     */
    public BigDecimal percentage(BigDecimal val){
        if(val == null){
            return null;
        }
        return val.multiply(BigDecimal.valueOf(100L));
    }
}
