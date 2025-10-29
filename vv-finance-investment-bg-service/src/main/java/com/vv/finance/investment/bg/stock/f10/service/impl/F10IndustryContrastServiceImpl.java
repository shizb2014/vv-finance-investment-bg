package com.vv.finance.investment.bg.stock.f10.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.constants.omdc.Adjhkt;
import com.vv.finance.common.constants.omdc.OmdcCommonConstant;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.f10.ComEstimationVO;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.EstimationSortEnum;
import com.vv.finance.investment.bg.entity.f10.SubBusinessInfo;
import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import com.vv.finance.investment.bg.entity.f10.enums.GrowthContrastSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.ScaleSortEnum;
import com.vv.finance.investment.bg.entity.f10.industry.Estimation;
import com.vv.finance.investment.bg.entity.f10.industry.GrowthContrast;
import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
import com.vv.finance.investment.bg.entity.f10.industry.Scale;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;
import com.vv.finance.investment.bg.stock.f10.handler.F10SourceHandlerV2;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName F10IndustryContrastServiceImpl
 * @Deacription f10 行业对比
 * @Author lh.sz
 * @Date 2021年08月19日 11:37
 **/
@Service
@Slf4j
public class F10IndustryContrastServiceImpl extends AbstractBaseServiceImpl {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private Xnhk0203Mapper xnhk0203Mapper;
    @Resource
    private Xnhk0206Mapper xnhk0206Mapper;
    @Resource
    private Xnhk0209Mapper xnhk0209Mapper;
    @Resource
    private Xnhk0406Mapper xnhk0406Mapper;
    @Resource
    private Xnhk0407Mapper xnhk0407Mapper;

    @Resource
    private F10SourceHandlerV2 f10SourceHandlerV2;

    @Resource
    private StockService stockService;

    @Resource
    private F10SourceServiceImpl f10SourceService;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Resource
    private StockCache stockCache;

    private static final String DATE_FORMATTER = "yyyy/MM/dd";

    private static final int RANK_NUM = 5;

    private List<List<MarketPresence>> updatePresenceBySnapshot(String code, List<List<MarketPresence>> presenceList) {
        log.info("F10IndustryContrast updatePresenceBySnapshot");
        if (CollUtil.isEmpty(presenceList)) {
            return Collections.emptyList();
        }
        // 最新1条记录
        // List<MarketPresence> marketPresences = presenceList.get(0);
        List<MarketPresence> marketPresences = CollUtil.getLast(presenceList);
        List<String> codes = marketPresences.stream().filter(mp -> StrUtil.isNotBlank(mp.getCode())).map(MarketPresence::getCode).collect(Collectors.toList());
        // 查询股票和指数快照
        List<StockSnapshot> snapshotList = stockService.getSnapshotList(Convert.toStrArray(codes));
        Map<String, StockSnapshot> codeSnapshotMap = snapshotList.stream().collect(Collectors.toMap(StockSnapshot::getCode, v -> v, (o, v) -> v));

        // 更新最后1条记录涨跌幅
        marketPresences.stream().forEach(lp -> {
            StockSnapshot snapshot = codeSnapshotMap.get(lp.getCode());
            lp.setTodayChgPct(getChgPct(snapshot, lp.getTodayChgPct(), StockSnapshot::getChgPct));
            lp.setFiveDayChgPct(getChgPct(snapshot, lp.getFiveDayChgPct(), StockSnapshot::getFiveDayChgPct));
            lp.setWeekChgPct(getChgPct(snapshot, lp.getWeekChgPct(), StockSnapshot::getWeeklyChgPct));
            lp.setMonthChgPct(getChgPct(snapshot, lp.getMonthChgPct(), StockSnapshot::getThirtyDayChgPct));
            lp.setNearThreeMonthChgPct(getChgPct(snapshot, lp.getNearThreeMonthChgPct(), StockSnapshot::getAlmostThreeMonthChgPct));
            lp.setNearSixMonthChgPct(getChgPct(snapshot, lp.getNearSixMonthChgPct(), StockSnapshot::getAlmostSixMonthChgPct));
            lp.setYearToDateChgPct(getChgPct(snapshot, lp.getYearToDateChgPct(), StockSnapshot::getYearToNowChgPct));
            lp.setNearOneYearChgPct(getChgPct(snapshot, lp.getNearOneYearChgPct(), StockSnapshot::getAlmostYearChgPct));
            lp.setNearTwoYearChgPct(getChgPct(snapshot, lp.getNearTwoYearChgPct(), StockSnapshot::getAlmostTwoYearChgPct));
            lp.setNearThreeYearChgPct(getChgPct(snapshot, lp.getNearThreeYearChgPct(), StockSnapshot::getAlmostThreeYearChgPct));
        });

        // 交易日当天，并和报价栏-所属行业保持一致
        SubBusinessInfo businessInfo = f10SourceService.subBusinessInfo(code);
        if (ObjectUtil.isNotEmpty(businessInfo)) {
            Opt.ofNullable(CollUtil.getLast(marketPresences)).ifPresent(mp -> mp.setTodayChgPct(BigDecimalUtil.getMultiply100Result(businessInfo.getIncrease())));
        }

        List<List<MarketPresence>> outputList = CollUtil.unionAll(CollUtil.sub(presenceList, 0, CollUtil.size(presenceList) - 1), ListUtil.of(marketPresences));

        return outputList;
    }

    private BigDecimal getChgPct(StockSnapshot stockSnapshot, BigDecimal marketChgPct, Function<StockSnapshot, BigDecimal> function) {
        if (ObjectUtil.isEmpty(stockSnapshot)) {
            // 如果快照为空，返回marketChgPct；marketChgPct 为空，返回0
            return BigDecimalUtil.isNullOrZero(marketChgPct) ? BigDecimal.ZERO : marketChgPct;
        } else {
            // 如果快照不为空，返回快照中的涨跌幅
            BigDecimal result = BigDecimalUtil.getMultiply100Result(function.apply(stockSnapshot));
            return BigDecimalUtil.isNullOrZero(result) ? BigDecimal.ZERO : result;
        }
    }

    /**
     * 获取规模对比
     *
     * @param code     股票代码
     * @param sortEnum 排序字段
     * @return
     */
    public List<Scale> getScale(String code,
                                ScaleSortEnum sortEnum) {
        List<Scale> scales = new LinkedList<>();
        List<Scale> list = buildScale(code);
        if (CollectionUtils.isEmpty(list)) {
            return scales;
        }
        //按不同的字段排序
        List<Scale> sortList = list.stream().filter(g -> ReflectUtil.getFieldValue(g, sortEnum.getDesc()) != null)
                .sorted(Comparator.comparing(s -> (BigDecimal) ReflectUtil.getFieldValue(s, sortEnum.getDesc()))
                        .reversed()).collect(Collectors.toList());
        //空字段也参与排序
        // 20240122 过滤空值
        // sortList.addAll(list.stream().filter(g -> ReflectUtil.getFieldValue(g, sortEnum.getDesc()) == null).collect(Collectors.toList()));
        sortList.forEach(s -> {
            s.setRank(sortList.indexOf(s) + 1);
            s.setType(1);
        });
        Scale avg = Scale.builder().build();
        avg.setName("行业平均");
        avg.setTotalValue(BigDecimal.valueOf(sortList.stream().filter(e -> e.getTotalValue() != null).mapToDouble(e -> e.getTotalValue().doubleValue()).average().orElse(0)));
        avg.setTotalAssets(BigDecimal.valueOf(sortList.stream().filter(e -> e.getTotalAssets() != null).mapToDouble(e -> e.getTotalAssets().doubleValue()).average().orElse(0)));
        avg.setTaking(BigDecimal.valueOf(sortList.stream().filter(e -> e.getTaking() != null).mapToDouble(e -> e.getTaking().doubleValue()).average().orElse(0)));
        avg.setGrossMargin(BigDecimal.valueOf(sortList.stream().filter(e -> e.getGrossMargin() != null).mapToDouble(e -> e.getGrossMargin().doubleValue()).average().orElse(0)));
        avg.setRetainedProfits(BigDecimal.valueOf(sortList.stream().filter(e -> e.getRetainedProfits() != null).mapToDouble(e -> e.getRetainedProfits().doubleValue()).average().orElse(0)));
        avg.setType(2);
        avg.setUpdateDate(list.get(0).getUpdateDate());
        Scale e = sortList.stream().filter(s -> code.equals(s.getCode())).findFirst().orElse(null);
        if (e == null){
            return scales;
        }
        scales.add(e);
        scales.add(avg);
        //设置中值
        scales.add(getScaleMid(list));
        int nowRank = scales.get(0).getRank();
        if (nowRank > RANK_NUM) {
            scales.addAll(sortList.subList(0, 5));
        } else {
            scales.addAll(sortList.subList(0, Math.min(CollUtil.size(sortList), 6)));
            scales.remove(nowRank + 2);
        }
        return scales;
    }

    /**
     * 获取规模对比中值
     *
     * @param list 规模对比数据
     * @return
     */
    private Scale getScaleMid(List<Scale> list) {
        BigDecimal totalValueMid = calcMid(list.stream().map(Scale::getTotalValue).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal totalAssetsMid = calcMid(list.stream().map(Scale::getTotalAssets).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal takingMid = calcMid(list.stream().map(Scale::getTaking).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal grossMarginMid = calcMid(list.stream().map(Scale::getGrossMargin).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal retainedProfitsMid = calcMid(list.stream().map(Scale::getRetainedProfits).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        return Scale
                .builder()
                .name("行业中值")
                .totalValue(totalValueMid)
                .totalAssets(totalAssetsMid)
                .taking(takingMid)
                .grossMargin(grossMarginMid)
                .retainedProfits(retainedProfitsMid)
                .type(2)
                .updateDate(list.get(0).getUpdateDate())
                .build();
    }

    /**
     * 构建规模对标
     *
     * @param code 股票代码
     * @return
     */
    private List<Scale> buildScale(String code) {
        List<Scale> list = new ArrayList<>();
        //获取该行业的所有股票
        List<String> codes = getIndustryCodes(code);
        if (CollectionUtils.isEmpty(codes)) {
            return list;
        }
        //获取股票名称
        Map<String, String> codeAndNameMap = getCodeAndName(codes);
        List<Xnhk0406> xnhk0406s = xnhk0406Mapper.selectList(new QueryWrapper<Xnhk0406>()
                .in("seccode", codes));
        List<Xnhk0407> xnhk0407s = xnhk0407Mapper.selectList(new QueryWrapper<Xnhk0407>()
                .in("seccode", codes));
        List<StockSnapshot> snapshotList = stockService.getSnapshotList(Convert.toStrArray(codes));
        Map<String, BigDecimal> totalValueMap = snapshotList.stream().filter(ss -> ObjectUtil.isNotEmpty(ss.getTotalValue())).collect(Collectors.toMap(StockSnapshot::getCode, StockSnapshot::getTotalValue, (o, v) -> v));
        if (CollectionUtils.isNotEmpty(xnhk0407s) && CollectionUtils.isNotEmpty(xnhk0406s)) {
            Xnhk0407 xnhk0407 = xnhk0407Mapper.selectOne(new QueryWrapper<Xnhk0407>().eq("seccode", code));
            xnhk0407s.forEach(x -> {
                Scale scale = Scale.builder().build();
                scale.setCode(x.getSeccode());
                scale.setName(codeAndNameMap.get(x.getSeccode()));
                BigDecimal totalValue = Objects.requireNonNull(xnhk0406s.stream().filter(s -> s.getSeccode().equals(x.getSeccode())).findAny().orElse(null)).getF008n();
                scale.setTotalValue(totalValueMap.getOrDefault(x.getSeccode(), totalValue));
                scale.setTotalAssets(x.getF011n());
                scale.setTaking(x.getF012n());
                scale.setGrossMargin(x.getF013n());
                scale.setRetainedProfits(x.getF014n());
                scale.setType(1);
                scale.setUpdateDate(xnhk0407 == null ? "" : DateUtils.formatDate(xnhk0407.getModifiedDate(), DATE_FORMATTER));
                list.add(scale);
            });
        }
        return list;
    }

    /**
     * 获取估值对比
     *
     * @param code     股票代码
     * @param sortEnum 排序字段
     * @return
     */
//    public List<Estimation> getEstimation(String code,
//                                          EstimationSortEnum sortEnum) {
//        List<Estimation> list = buildEstimation(code);
//        List<Estimation> estimations = new LinkedList<>();
//        if (CollectionUtils.isEmpty(list)) {
//            return estimations;
//        }
//        //按不同的字段排序
//        List<Estimation> sortList = list.stream().filter(g -> ReflectUtil.getFieldValue(g, sortEnum.getDesc()) != null)
//                .sorted(Comparator.comparing(s -> (BigDecimal) ReflectUtil.getFieldValue(s, sortEnum.getDesc()))
//                        .reversed()).collect(Collectors.toList());
//        //空字段也参与排序
//        sortList.addAll(list.stream().filter(g -> ReflectUtil.getFieldValue(g, sortEnum.getDesc()) == null).collect(Collectors.toList()));
//        sortList.forEach(s -> {
//            s.setRank(sortList.indexOf(s) + 1);
//            s.setType(1);
//        });
//        Estimation avg = Estimation.builder().build();
//        avg.setName("行业平均");
//        avg.setPeTtm(BigDecimal.valueOf(sortList.stream().filter(e -> e.getPeTtm() != null).mapToDouble(e -> e.getPeTtm().doubleValue()).average().orElse(0)));
//        avg.setPeLyr(BigDecimal.valueOf(sortList.stream().filter(e -> e.getPeLyr() != null).mapToDouble(e -> e.getPeLyr().doubleValue()).average().orElse(0)));
//        avg.setDividendYieldTtm(BigDecimal.valueOf(sortList.stream().filter(e -> e.getDividendYieldTtm() != null).mapToDouble(e -> e.getDividendYieldTtm().doubleValue()).average().orElse(0)));
//        avg.setDividendYieldLyr(BigDecimal.valueOf(sortList.stream().filter(e -> e.getDividendYieldLyr() != null).mapToDouble(e -> e.getDividendYieldLyr().doubleValue()).average().orElse(0)));
//        avg.setPb(BigDecimal.valueOf(sortList.stream().filter(e -> e.getPb() != null).mapToDouble(e -> e.getPb().doubleValue()).average().orElse(0)));
//        avg.setPrg(BigDecimal.valueOf(sortList.stream().filter(e -> e.getPrg() != null).mapToDouble(e -> e.getPrg().doubleValue()).average().orElse(0)));
//        avg.setRoe(BigDecimal.valueOf(sortList.stream().filter(e -> e.getRoe() != null).mapToDouble(e -> e.getRoe().doubleValue()).average().orElse(0)));
//        avg.setRoa(BigDecimal.valueOf(sortList.stream().filter(e -> e.getRoa() != null).mapToDouble(e -> e.getRoa().doubleValue()).average().orElse(0)));
//        avg.setType(2);
//        avg.setUpdateDate(list.get(0).getUpdateDate());
//        Estimation e = sortList.stream().filter(s -> code.equals(s.getCode())).findFirst().orElse(null);
//        if (e == null) {
//            return estimations;
//        }
//        estimations.add(e);
//        estimations.add(avg);
//        estimations.add(getEstimationMid(list));
//        int nowRank = e.getRank();
//        if (nowRank > RANK_NUM) {
//            estimations.addAll(sortList.subList(0, 5));
//        } else {
//            estimations.addAll(sortList.subList(0, 6));
//            estimations.remove(nowRank + 2);
//        }
//        return estimations;
//    }


    /**
     * 构建估值信息
     *
     * @param code 股票代码
     * @return
     */
    public List<Estimation> buildEstimationV2(String code) {
        List<Estimation> list = new ArrayList<>();
//        Date updateDate = xnhk0406.getModifiedDate();
        //获取该行业的所有股票
        List<String> codes = getIndustryCodesV2(code);
        if (CollectionUtils.isEmpty(codes)) {
            return list;
        }
        //获取股票名称
        Map<String, String> codeAndNameMap = stockCache.queryStockNameMap(null);
        List<Xnhk0406> xnhk0406s = xnhk0406Mapper.selectList(new QueryWrapper<Xnhk0406>().
                in("seccode", codes));
        if (CollectionUtils.isNotEmpty(xnhk0406s)) {
            Xnhk0406 xnhk0406 = xnhk0406Mapper.selectOne(new QueryWrapper<Xnhk0406>().eq("seccode", code));
            xnhk0406s.forEach(x -> {
                Estimation estimation = Estimation.builder().build();
                estimation.setCode(x.getSeccode());
                estimation.setName(codeAndNameMap.get(x.getSeccode()));
                estimation.setPeTtm(x.getF009n());
                estimation.setPeLyr(x.getF010n());
                estimation.setDividendYieldTtm(x.getF011n());
                estimation.setDividendYieldLyr(x.getF012n());
                estimation.setPb(x.getF013n());
                estimation.setPrg(x.getF014n());
                estimation.setRoe(x.getF015n());
                estimation.setRoa(x.getF016n());
                estimation.setUpdateDate(xnhk0406 == null ? "" : DateUtils.formatDate(xnhk0406.getModifiedDate(), DATE_FORMATTER));
                list.add(estimation);
            });
        }
        return list;
    }


    /**
     * 构建估值信息
     *
     * @param code 股票代码
     * @return
     */
    private List<Estimation> buildEstimation(String code) {
        List<Estimation> list = new ArrayList<>();
//        Date updateDate = xnhk0406.getModifiedDate();
        //获取该行业的所有股票
        List<String> codes = getIndustryCodes(code);
        if (CollectionUtils.isEmpty(codes)) {
            return list;
        }
        //获取股票名称
        Map<String, String> codeAndNameMap = getCodeAndName(codes);
        List<Xnhk0406> xnhk0406s = xnhk0406Mapper.selectList(new QueryWrapper<Xnhk0406>().
                in("seccode", codes));
        if (CollectionUtils.isNotEmpty(xnhk0406s)) {
            Xnhk0406 xnhk0406 = xnhk0406Mapper.selectOne(new QueryWrapper<Xnhk0406>().eq("seccode", code));
            xnhk0406s.forEach(x -> {
                Estimation estimation = Estimation.builder().build();
                estimation.setCode(x.getSeccode());
                estimation.setName(codeAndNameMap.get(x.getSeccode()));
                estimation.setPeTtm(x.getF009n());
                estimation.setPeLyr(x.getF010n());
                estimation.setDividendYieldTtm(x.getF011n());
                estimation.setDividendYieldLyr(x.getF012n());
                estimation.setPb(x.getF013n());
                estimation.setPrg(x.getF014n());
                estimation.setRoe(x.getF015n());
                estimation.setRoa(x.getF016n());
                estimation.setUpdateDate(xnhk0406 == null ? "" : DateUtils.formatDate(xnhk0406.getModifiedDate(), DATE_FORMATTER));
                list.add(estimation);
            });
        }
        return list;
    }



    /**
     * 成长性对比
     *
     * @param code                   股票代码
     * @param growthContrastSortEnum 排序字段
     * @return
     */
    public List<GrowthContrast> getGrowthContrast(String code, GrowthContrastSortEnum growthContrastSortEnum) {
        //构建所有的成长对比信息
        List<GrowthContrast> list = builderGrowthContrast(code);
        List<GrowthContrast> growthContrasts = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return growthContrasts;
        }
        List<GrowthContrast> sortList = list.stream().filter(g -> ReflectUtil.getFieldValue(g, growthContrastSortEnum.getDesc()) != null)
                .sorted(Comparator.comparing(growthContrast -> (BigDecimal) ReflectUtil.getFieldValue(growthContrast, growthContrastSortEnum.getDesc()))
                        .reversed()).collect(Collectors.toList());
        // 20240122 过滤空值
        // sortList.addAll(list.stream().filter(g -> ReflectUtil.getFieldValue(g, growthContrastSortEnum.getDesc()) == null).collect(Collectors.toList()));
        sortList.forEach(s -> {
            s.setRank(sortList.indexOf(s) + 1);
            s.setType(1);
        });
        GrowthContrast avg = GrowthContrast.builder()
                .type(2)
                .name("行业平均")
                .earningsPerShareGrowthRate(BigDecimal.valueOf(sortList.stream().filter(g -> g.getEarningsPerShareGrowthRate() != null).
                        mapToDouble(g -> g.getEarningsPerShareGrowthRate().doubleValue()).average().orElse(0)))
                .mbrg(BigDecimal.valueOf(sortList.stream().filter(g -> g.getMbrg() != null).
                        mapToDouble(g -> g.getMbrg().doubleValue()).average().orElse(0)))
                .operatingProfitGrowthRate(BigDecimal.valueOf(sortList.stream().filter(g -> g.getOperatingProfitGrowthRate() != null).
                        mapToDouble(g -> g.getOperatingProfitGrowthRate().doubleValue()).average().orElse(0)))
                .growthRateTotalAssets(BigDecimal.valueOf(sortList.stream().filter(g -> g.getGrowthRateTotalAssets() != null).
                        mapToDouble(g -> g.getGrowthRateTotalAssets().doubleValue()).average().orElse(0)))
                .updateDate(list.get(0).getUpdateDate())
                .build();
        GrowthContrast e = sortList.stream().filter(s -> code.equals(s.getCode())).findFirst().orElse(null);
        if (e == null){
            return growthContrasts;
        }
        growthContrasts.add(e);
        growthContrasts.add(avg);
        //获取成长性对比中值
        growthContrasts.add(getGrowthMid(list));
        int nowRank = growthContrasts.get(0).getRank();
        if (nowRank > RANK_NUM) {
            growthContrasts.addAll(sortList.subList(0, 5));
        } else {
            growthContrasts.addAll(sortList.size() >= 6 ? sortList.subList(0, 6) : sortList);
            growthContrasts.remove(nowRank + 2);
        }
        return growthContrasts;
    }

    /**
     * 获取成长性对比中值
     *
     * @param list
     * @return
     */
    private GrowthContrast getGrowthMid(List<GrowthContrast> list) {
        BigDecimal midEar = calcMid(list.stream().map(GrowthContrast::getEarningsPerShareGrowthRate).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal midMbrg = calcMid(list.stream().map(GrowthContrast::getMbrg).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal midOpr = calcMid(list.stream().map(GrowthContrast::getOperatingProfitGrowthRate).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        BigDecimal midGrow = calcMid(list.stream().map(GrowthContrast::getGrowthRateTotalAssets).filter(Objects::nonNull).sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        return GrowthContrast
                .builder()
                .type(2)
                .name("行业中值")
                .earningsPerShareGrowthRate(midEar)
                .mbrg(midMbrg)
                .operatingProfitGrowthRate(midOpr)
                .growthRateTotalAssets(midGrow)
                .build();
    }

    /**
     * 构建所有的成长对比信息
     *
     * @param code 股票代码
     * @return
     */
    private List<GrowthContrast> builderGrowthContrast(String code) {
        List<GrowthContrast> list = new ArrayList<>();
        int marketType = getMarketType(code);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null == f10MarketTypeEnum) {
            return list;
        }

        //获取行业下的所有codes
        List<String> codes = getIndustryCodes(code);
        if (CollectionUtils.isEmpty(codes)){
            return list;
        }
        //获取code,名称
        Map<String, String> codeAndNameMap = getCodeAndName(codes);
        //获取股票的市场类型
        // List<Xnhks0101> xnhks0101s = getType(codes);
        Map<String, Integer> hashMap = redisClient.hmget(RedisKeyConstants.STOCK_FINANCE_TYPE);
        Map<String, Integer> filterMap = MapUtil.filter(hashMap, Convert.toStrArray(codes));
        Set<String> codeSet = MapUtil.filter(filterMap, entry -> ObjectUtil.equal(entry.getValue(), marketType)).keySet();

        String endDate;
        switch (f10MarketTypeEnum) {
            // 20231219 成长性对比计算取最新一期年报数据
            case NO_FINANCIAL:
                Xnhk0203 xnhk0203 = xnhk0203Mapper.selectOne(new QueryWrapper<Xnhk0203>().select("F002D").eq("seccode", code).eq("F006V", "F").orderByDesc("f002D").last(" limit 1"));
                endDate = ObjectUtils.isEmpty(xnhk0203) ? null : DateUtils.formatDate(DateUtils.parseDate(xnhk0203.getF002d()), DATE_FORMATTER);
                list = getNonFinancialList(endDate, codeSet, codeAndNameMap);
                break;
            case FINANCIAL:
                Xnhk0206 xnhk0206 = xnhk0206Mapper.selectOne(new QueryWrapper<Xnhk0206>().select("F002D").eq("seccode", code).eq("F006V", "F").orderByDesc("f002D").last(" limit 1"));
                endDate = ObjectUtils.isEmpty(xnhk0206) ? null : DateUtils.formatDate(DateUtils.parseDate(xnhk0206.getF002d()), DATE_FORMATTER);
                list = getFinancialList(endDate, codeSet, codeAndNameMap);
                break;
            case INSURANCE:
                Xnhk0209 xnhk0209 = xnhk0209Mapper.selectOne(new QueryWrapper<Xnhk0209>().select("F002D").eq("seccode", code).eq("F006V", "F").orderByDesc("f002D").last(" limit 1"));
                endDate = ObjectUtils.isEmpty(xnhk0209) ? null : DateUtils.formatDate(DateUtils.parseDate(xnhk0209.getF002d()), DATE_FORMATTER);
                list = getInsuranceList(endDate, codeSet, codeAndNameMap);
                break;
            default:
                break;
        }

        return list;
    }

    private List<GrowthContrast> getNonFinancialList(String endDate, Set<String> codeSet, Map<String, String> codeAndNameMap) {
        if (ObjectUtils.isEmpty(endDate)) {
            return Collections.emptyList();
        }
        Query noFinQuery = Query.query(Criteria.where("stockCode")
                .in(codeSet)
                .and("reportType").is(ReportTypeEnum.F.getCode()));
        List<F10KeyFiguresNonFinancialEntity> nonFinancialIndexes = mongoTemplate.find(noFinQuery, F10KeyFiguresNonFinancialEntity.class);
        List<F10NoFinProfitEntity> nonFinancialProfits = mongoTemplate.find(noFinQuery, F10NoFinProfitEntity.class);
        //分组并选择最新的年报数据
        nonFinancialIndexes = nonFinancialIndexes.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        nonFinancialProfits = nonFinancialProfits.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        // 20231219 评审意见为 无保留意见 (1,2)
        List<String> nonAuditCodes = nonFinancialProfits.stream().filter(fp -> ListUtil.of("1", "2").contains(fp.getAuditOpinion())).map(fp -> fp.getStockCode()).collect(Collectors.toList());

        return nonFinancialIndexes.stream().filter(fi -> nonAuditCodes.contains(fi.getStockCode())).map(noFin -> {
            GrowthContrast growthContrast = GrowthContrast.builder().build();
            growthContrast.setCode(noFin.getStockCode());
            growthContrast.setName(codeAndNameMap.get(noFin.getStockCode()));
            //每股盈利增长率
            growthContrast.setEarningsPerShareGrowthRate(noFin.getGrowthAbility() == null || noFin.getGrowthAbility().getEarningPerShareGrowth() == null ? null :
                    noFin.getGrowthAbility().getEarningPerShareGrowth().getVal());

            //营业收入增长率
            growthContrast.setMbrg(noFin.getGrowthAbility().getOperatingRevenueGrowth() == null || noFin.getGrowthAbility() == null ? null :
                    noFin.getGrowthAbility().getOperatingRevenueGrowth().getVal());

            //经营溢利增长率
            growthContrast.setOperatingProfitGrowthRate(noFin.getGrowthAbility().getGrossIncomeGrowth() == null || noFin.getGrowthAbility() == null ? null :
                    noFin.getGrowthAbility().getGrossIncomeGrowth().getVal());
            //总资产增长率
            growthContrast.setGrowthRateTotalAssets(noFin.getGrowthAbility() == null || noFin.getGrowthAbility().getTotalAssetsGrowth() == null ? null :
                    noFin.getGrowthAbility().getTotalAssetsGrowth().getVal());
            //TODO-luoyj 净利润增长率？netProfitGrowth
            growthContrast.setType(1);
            growthContrast.setUpdateDate(endDate);
            return growthContrast;
        }).collect(Collectors.toList());
    }

    private List<GrowthContrast> getFinancialList(String endDate, Set<String> codeSet, Map<String, String> codeAndNameMap) {
        if (ObjectUtils.isEmpty(endDate)) {
            return Collections.emptyList();
        }
        Query finQuery = Query.query(Criteria.where("stockCode")
                .in(codeSet)
                .and("reportType").is(ReportTypeEnum.F.getCode()));
        List<F10KeyFiguresFinancialEntity> financialIndexes = mongoTemplate.find(finQuery, F10KeyFiguresFinancialEntity.class);
        List<F10FinProfitEntity> financialProfits = mongoTemplate.find(finQuery, F10FinProfitEntity.class);
        //分组并选择最新的年报数据
        financialIndexes = financialIndexes.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        financialProfits = financialProfits.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        // 20231219 评审意见为 无保留意见 (1,2)
        List<String> nonAuditCodes = financialProfits.stream().filter(fp -> ListUtil.of("1", "2").contains(fp.getAuditOpinion())).map(fp -> fp.getStockCode()).collect(Collectors.toList());

        return financialIndexes.stream().filter(fi -> nonAuditCodes.contains(fi.getStockCode())).map(fin -> {
            GrowthContrast growthContrast = GrowthContrast.builder().build();
            growthContrast.setCode(fin.getStockCode());
            growthContrast.setName(codeAndNameMap.get(fin.getStockCode()));
            growthContrast.setEarningsPerShareGrowthRate(fin.getGrowthAbility() == null || fin.getGrowthAbility().getEarningPerShareGrowth() == null ? null :
                    fin.getGrowthAbility().getEarningPerShareGrowth().getVal());
            growthContrast.setMbrg(fin.getGrowthAbility().getOperatingRevenueGrowth() == null || fin.getGrowthAbility() == null ? null :
                    fin.getGrowthAbility().getOperatingRevenueGrowth().getVal());
            growthContrast.setOperatingProfitGrowthRate(fin.getGrowthAbility().getGrossIncomeGrowth() == null || fin.getGrowthAbility() == null ? null :
                    fin.getGrowthAbility().getGrossIncomeGrowth().getVal());
            growthContrast.setGrowthRateTotalAssets(fin.getGrowthAbility() == null || fin.getGrowthAbility().getTotalAssetsGrowth() == null ? null :
                    fin.getGrowthAbility().getTotalAssetsGrowth().getVal());
            growthContrast.setType(1);
            growthContrast.setUpdateDate(endDate);
            return growthContrast;
        }).collect(Collectors.toList());
    }

    private List<GrowthContrast> getInsuranceList(String endDate, Set<String> codeSet, Map<String, String> codeAndNameMap) {
        if (ObjectUtils.isEmpty(endDate)) {
            return Collections.emptyList();
        }
        Query inQuery = Query.query(Criteria.where("stockCode")
                .in(codeSet)
                .and("reportType").is(ReportTypeEnum.F.getCode()));
        List<F10KeyFiguresInsuranceEntity> insuranceIndexes = mongoTemplate.find(inQuery, F10KeyFiguresInsuranceEntity.class);
        List<F10InsureProfitEntity> insuranceProfits = mongoTemplate.find(inQuery, F10InsureProfitEntity.class);
        //分组并选择最新的年报数据
        insuranceIndexes = insuranceIndexes.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        insuranceProfits = insuranceProfits.stream().collect(Collectors.groupingBy(item -> item.getStockCode(), Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(item -> item.getEndDate())),Optional::get))).values().stream().collect(Collectors.toList());
        // 20231219 评审意见为 无保留意见 (1,2)
        List<String> nonAuditCodes = insuranceProfits.stream().filter(fp -> ListUtil.of("1", "2").contains(fp.getAuditOpinion())).map(fp -> fp.getStockCode()).collect(Collectors.toList());

        return insuranceIndexes.stream().filter(fi -> nonAuditCodes.contains(fi.getStockCode())).map(in -> {
            GrowthContrast growthContrast = GrowthContrast.builder().build();
            growthContrast.setCode(in.getStockCode());
            growthContrast.setName(codeAndNameMap.get(in.getStockCode()));
            growthContrast.setEarningsPerShareGrowthRate(in.getGrowthAbility() == null || in.getGrowthAbility().getEarningPerShareGrowth() == null ? null :
                    in.getGrowthAbility().getEarningPerShareGrowth().getVal());
            growthContrast.setMbrg(in.getGrowthAbility().getOperatingRevenueGrowth() == null || in.getGrowthAbility() == null ? null :
                    in.getGrowthAbility().getOperatingRevenueGrowth().getVal());
            growthContrast.setOperatingProfitGrowthRate(in.getGrowthAbility().getGrossIncomeGrowth() == null || in.getGrowthAbility() == null ? null :
                    in.getGrowthAbility().getGrossIncomeGrowth().getVal());
            growthContrast.setGrowthRateTotalAssets(
                    in.getGrowthAbility() == null || in.getGrowthAbility().getTotalAssetsGrowth() == null ? null :
                            in.getGrowthAbility().getTotalAssetsGrowth().getVal());
            growthContrast.setType(1);
            growthContrast.setUpdateDate(endDate);
            return growthContrast;
        }).collect(Collectors.toList());
    }

}
