package com.vv.finance.investment.bg.api.impl.uts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.investment.bg.api.uts.IMainBusinessService;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.AreaVal;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.BusVal;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.MainBusinessData;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.YearBusVal;
import com.vv.finance.investment.bg.entity.uts.Xnhks0110;
import com.vv.finance.investment.bg.entity.uts.Xnhks0111;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0110Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0111Mapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: wsliang
 * @Date: 2021/9/1 10:07
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class MainBusinessServiceImpl implements IMainBusinessService {

    @Resource
    private Xnhks0110Mapper xnhks0110Mapper;

    @Resource
    private Xnhks0111Mapper xnhks0111Mapper;

    @Value("${mainBusiness.businessColors}")
    private List<String> colors;

    private final List<String> CHINESE_AREAS = Arrays.asList(new String[]{"香港", "台湾", "澳门"});
    private final String CHINESE_PREFIX = "中国";

    private final BigDecimal MAX_POP = new BigDecimal(0.0005);
    private final BigDecimal NEG_MAX_POP = new BigDecimal(-0.0005);
    private final String OTHER_NAME = "其他";
    private final Integer MAX_SIZE = 25;

    @Override
    public MainBusinessData getBusinessData(String stockCode) {
        List<String> areas = new ArrayList<>();
        List<Xnhks0111> xnhks0111s = CollectionUtil.emptyIfNull(xnhks0111Mapper.listModel(stockCode));
        List<String> business = new ArrayList<>();
        List<Xnhks0110> xnhks0110s = CollectionUtil.emptyIfNull(xnhks0110Mapper.listModel(stockCode));

        MainBusinessData mainBusinessData = new MainBusinessData();
        if (CollectionUtil.isEmpty(xnhks0110s) && CollectionUtil.isEmpty(xnhks0111s)) {
            return null;
        }

        Long x110MaxYear = CollectionUtil.isEmpty(xnhks0110s) ? 0 : xnhks0110s.get(xnhks0110s.size() - 1).getF003d();
        Long x111MaxYear = CollectionUtil.isEmpty(xnhks0111s) ? 0 : xnhks0111s.get(xnhks0111s.size() - 1).getF003d();
        Long maxYear = x110MaxYear > x111MaxYear ? x110MaxYear : x111MaxYear;
        AtomicLong year = new AtomicLong(maxYear);
        List<YearBusVal> yearBusVals = new ArrayList<>(10);
        List<BusVal> busValList;
        List<AreaVal> areaValList;
        String busCurrency = CollectionUtil.isEmpty(xnhks0110s) ? null : xnhks0110s.get(xnhks0110s.size() - 1).getF005v();
        mainBusinessData.setBusCurrency(busCurrency);

        String areaCurrency = CollectionUtil.isEmpty(xnhks0111s) ? null : xnhks0111s.get(xnhks0111s.size() - 1).getF005v();
        mainBusinessData.setAreaCurrency(areaCurrency);

        // 20231213 按最新年度的营业额倒序排列，其中营业额相等时按类型名称的字母顺序正序排列，营业额为--的排在最后且按类型名称的字母顺序正序排列
        Comparator<BusVal> busComparator = Comparator.comparing(BusVal::getTurnover, Comparator.nullsFirst(BigDecimal::compareTo)).reversed().thenComparing(f -> PinyinUtil.getFirstLetter(f.getBusName(), ""));
        Comparator<AreaVal> areaComparator = Comparator.comparing(AreaVal::getTurnover, Comparator.nullsFirst(BigDecimal::compareTo)).reversed().thenComparing(f -> PinyinUtil.getFirstLetter(f.getAreaName(), ""));
        Comparator<String> nameComparator = Comparator.comparing(f -> PinyinUtil.getFirstLetter(f, ""));

        for (int i = 0; i < 10; i++) {
            // 根据业务
            if (CollectionUtil.isNotEmpty(xnhks0110s)) {
                Supplier<Stream<Xnhks0110>> busSupplier = () -> xnhks0110s.stream().filter(xnhks0110 -> year.get() == xnhks0110.getF003d());
                BigDecimal sumBus = busSupplier.get().map(Xnhks0110::getF009n).filter(item -> ObjectUtils.isNotEmpty(item)).reduce(BigDecimal.ZERO, BigDecimal::add);
                busValList = busSupplier.get().map(xnhks0110 -> {
                    return BusVal.builder().busName(xnhks0110.getF008v())
                            .turnover(xnhks0110.getF009n())
                            .sumTurnover(sumBus)
                            .pop(ObjectUtils.isEmpty(xnhks0110.getF009n()) ? BigDecimal.ZERO : BigDecimalUtil.divideReturnZero(xnhks0110.getF009n(),sumBus,6))
                            .currency(xnhks0110.getF005v())
                            .build();
                }).sorted(busComparator).collect(Collectors.toList());
                busValList = ab(busValList, sumBus);
                business.addAll(busValList.stream().map(BusVal::getBusName).collect(Collectors.toSet()));
                // currency = busSupplier.get().findAny().orElse(new Xnhks0110()).getF005v();
            } else {
                busValList = Collections.emptyList();
            }
            // 根据地区
            if (CollectionUtil.isNotEmpty(xnhks0111s)) {
                Supplier<Stream<Xnhks0111>> areaSupplier = () -> xnhks0111s.stream().filter(xnhks0111 -> year.get() == xnhks0111.getF003d());
                BigDecimal sumArea = areaSupplier.get().map(Xnhks0111::getF009n).filter(item -> ObjectUtils.isNotEmpty(item)).reduce(BigDecimal.ZERO, BigDecimal::add);
                areaValList = areaSupplier.get().map(xnhks0111 -> {
                    return AreaVal.builder().areaName(addPrefix(xnhks0111.getF008v()))
                            .turnover(xnhks0111.getF009n())
                            .sumTurnover(sumArea)
                            .pop(ObjectUtils.isEmpty(xnhks0111.getF009n()) ? BigDecimal.ZERO : BigDecimalUtil.divideReturnZero(xnhks0111.getF009n(),sumArea,6))
                            .currency(xnhks0111.getF005v())
                            .build();
                }).sorted(areaComparator).collect(Collectors.toList());
                areaValList = aa(areaValList, sumArea);
                areas.addAll(areaValList.stream().map(AreaVal::getAreaName).collect(Collectors.toSet()));
                // String areaCurrency = areaSupplier.get().findAny().orElse(new Xnhks0111()).getF005v();
                // currency = StrUtil.blankToDefault(currency, areaCurrency);
            } else {
                areaValList = Collections.emptyList();
            }

            YearBusVal build = YearBusVal.builder().busValList(busValList).areaValList(areaValList).year(year.toString()).build();
            yearBusVals.add(build);
            year.decrementAndGet();
        }
        CollectionUtil.reverse(yearBusVals);

        areas = CollUtil.distinct(areas);
        business = CollUtil.distinct(business);

        // 如果最新一条记录没有币种，用上一条有币种记录的币种补点
        // 1. 2023只有业务，没有地区；需要使用2022地区对2023地区补点；
        // 2. 2023只有业务，没有地区；2022也无地区，使用2021地区对2023地区补点；
        // 3. 2023有业务和地区，地区中币种为空；使用2022币种对2023补点；
        if (CollUtil.isNotEmpty(yearBusVals)) {
            YearBusVal lastYearVal = CollUtil.getLast(yearBusVals);
            String lastYear = lastYearVal.getYear();
            List<AreaVal> lastYearAreas = CollUtil.defaultIfEmpty(lastYearVal.getAreaValList(), ListUtil.of(AreaVal.builder().build()));
            List<BusVal> lastYearBuses = CollUtil.defaultIfEmpty(lastYearVal.getBusValList(), ListUtil.of(BusVal.builder().build()));
            if (lastYearAreas.stream().anyMatch(ur -> StrUtil.isBlank(ur.getCurrency()))) {
                List<String> areaCurrencies = yearBusVals.stream()
                        .filter(ur -> CompareUtil.compare(ur.getYear(), lastYear) < 0 && CollUtil.isNotEmpty(ur.getAreaValList()))
                        .map(ur -> ur.getAreaValList().stream().map(AreaVal::getCurrency).filter(StrUtil::isNotBlank).collect(Collectors.toList()))
                        .reduce(new ArrayList<>(), CollUtil::unionAll);
                // 以最后一个币种补点
                lastYearAreas.stream().filter(ur -> StrUtil.isBlank(ur.getCurrency())).forEach(ur -> ur.setCurrency(CollUtil.getLast(areaCurrencies)));
                lastYearVal.setAreaValList(lastYearAreas);
            }
            if (lastYearBuses.stream().anyMatch(ub -> StrUtil.isBlank(ub.getCurrency()))) {
                List<String> areaCurrencies = yearBusVals.stream()
                        .filter(ub -> CompareUtil.compare(ub.getYear(), lastYear) < 0 && CollUtil.isNotEmpty(ub.getBusValList()))
                        .map(ub -> ub.getBusValList().stream().map(BusVal::getCurrency).filter(StrUtil::isNotBlank).collect(Collectors.toList()))
                        .reduce(new ArrayList<>(), CollUtil::unionAll);
                // 以最后一个币种补点
                lastYearBuses.stream().filter(ub -> StrUtil.isBlank(ub.getCurrency())).forEach(ur -> ur.setCurrency(CollUtil.getLast(areaCurrencies)));
                lastYearVal.setBusValList(lastYearBuses);
            }
        }

        // 20231213 按最新年度的营业额倒序排列，其中营业额相等时按类型名称的字母顺序正序排列，营业额为--的排在最后且按类型名称的字母顺序正序排列
        YearBusVal maxYearBus = yearBusVals.stream().max(Comparator.comparing(YearBusVal::getYear)).orElseGet(YearBusVal::new);
        if (CollUtil.isNotEmpty(maxYearBus.getAreaValList())) {
            // 根据营业排序
            List<String> sortedAreas = maxYearBus.getAreaValList().stream().sorted(areaComparator).map(AreaVal::getAreaName).collect(Collectors.toList());
            // 取交集
            Collection<String> intersection = CollUtil.intersection(areas, sortedAreas);
            // 对交集中记录排序
            List<String> sortedIntersection = intersection.stream().sorted(Comparator.comparingInt(sortedAreas::indexOf)).collect(Collectors.toCollection(ArrayList::new));
            // 其他按字母顺序排序
            List<String> otherAreas = CollUtil.subtract(areas, sortedIntersection).stream().sorted(nameComparator).collect(Collectors.toList());
            // 合并
            sortedIntersection.addAll(otherAreas);
            areas = sortedIntersection;
        }
        if (CollUtil.isNotEmpty(maxYearBus.getBusValList())) {
            // 根据营业排序
            List<String> sortedBuses = maxYearBus.getBusValList().stream().sorted(busComparator).map(BusVal::getBusName).collect(Collectors.toList());
            // 取交集
            Collection<String> intersection = CollUtil.intersection(business, sortedBuses);
            // 对交集中记录排序
            List<String> sortedIntersection = intersection.stream().sorted(Comparator.comparingInt(sortedBuses::indexOf)).collect(Collectors.toCollection(ArrayList::new));
            // 其他按字母顺序排序
            List<String> otherBuses = CollUtil.subtract(business, sortedIntersection).stream().sorted(nameComparator).collect(Collectors.toList());
            // 合并
            sortedIntersection.addAll(otherBuses);
            business = sortedIntersection;
        }

        mainBusinessData.setYearBusVals(yearBusVals);
        Date busDate = xnhks0110Mapper.maxModifiedDate(stockCode);
        mainBusinessData.setBusDate(busDate == null ? null : busDate.getTime());
        Date areaDate = xnhks0111Mapper.maxModifiedDate(stockCode);
        mainBusinessData.setAreaDate(areaDate == null ? null : areaDate.getTime());
        mainBusinessData.setAreas(areas);
        mainBusinessData.setBusiness(business);
        return mainBusinessData;
    }

    /**
     * 中国地区加上中国前缀
     *
     * @param area
     * @return
     */
    private String addPrefix(String area) {
        if (CHINESE_AREAS.contains(area)) {
            return CHINESE_PREFIX + area;
        }
        return area;
    }

    /**
     * 将小于0.05%的数据合并到其他
     *
     * @param list
     * @return
     */
    private List<AreaVal> aa(List<AreaVal> list, BigDecimal sumArea) {
        List<AreaVal> resultList;
        List<AreaVal> others = list.stream().filter(area -> {
            return (area.getPop().compareTo(MAX_POP) <= 0 && area.getPop().compareTo(NEG_MAX_POP) >= 0) || OTHER_NAME.equals(area.getAreaName());
        }).collect(Collectors.toList());
        // list.removeAll(others);
        list = CollUtil.subtractToList(list, others);
        if (list.size() >= MAX_SIZE) {
            resultList = list.subList(0, MAX_SIZE);
            // list.removeAll(resultList);
            list = CollUtil.subtractToList(list, resultList);
            others.addAll(list);
        } else {
            resultList = list;
        }
        if (CollectionUtil.isNotEmpty(others)) {
            BigDecimal otherPop = others.stream().map(AreaVal::getPop).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal otherTurnover = others.stream().map(AreaVal::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add);
            AreaVal other = AreaVal.builder().areaName(OTHER_NAME).pop(otherPop).sumTurnover(sumArea).turnover(otherTurnover).build();
            resultList.add(other);
        }
        return resultList;
    }

    private List<BusVal> ab(List<BusVal> list, BigDecimal sumBus) {
        List<BusVal> resultList;
        List<BusVal> others = list.stream().filter(area -> {
            return (area.getPop().compareTo(MAX_POP) <= 0 && area.getPop().compareTo(NEG_MAX_POP) >= 0) || OTHER_NAME.equals(area.getBusName());
        }).collect(Collectors.toList());
        // list.removeAll(others);
        list = CollUtil.subtractToList(list, others);
        if (list.size() >= MAX_SIZE) {
            resultList = list.subList(0, MAX_SIZE);
            // list.removeAll(resultList);
            list = CollUtil.subtractToList(list, resultList);
            others.addAll(list);
        } else {
            resultList = list;
        }
        if (CollectionUtil.isNotEmpty(others)) {
            BigDecimal otherPop = others.stream().map(BusVal::getPop).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal otherTurnover = others.stream().map(BusVal::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add);
            BusVal other = BusVal.builder().busName(OTHER_NAME).pop(otherPop).sumTurnover(sumBus).turnover(otherTurnover).build();
            resultList.add(other);
        }
        return resultList;
    }

    @Override
    public List<String> getBusinessSort() {
        return colors;
    }

}
