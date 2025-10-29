package com.vv.finance.investment.bg.handler.stock;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.ComXnhk0127Dto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.dto.ComRankShortSale;
import com.vv.finance.common.enums.ComStockStatueEnum;
import com.vv.finance.common.enums.SortEnum;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.KlineCalcUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk0603;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0603Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.stock.info.dto.StockShortSale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName StockHandler
 * @Deacription 股票相关处理类
 * @Author lh.sz
 * @Date 2021年10月09日 17:15
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class StockHandler {
    @Resource
    Xnhk0603Mapper xnhk0603Mapper;
    @Resource
    Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;
    @Resource
    StockService stockService;
    @Resource
    UtsInfoService utsInfoService;
    @Resource
    RedisClient redisClient;
    @Resource
    private StockCache stockCache;
    /**
     * 获取六个月的沽空数据
     *
     * @param stockCode 股票代码
     * @return List<StockShortSale>
     */
    public List<Xnhk0603> getStockShortSale(String stockCode) {
        //获取临时股票数据，判断股票是否是临时股票，若是临时股票则沽空数据需过滤掉当前并行交易前的数据
        List<ReuseTempDTO> tradingTempStocks = utsInfoService.findTradingTempStockByTime(new Date());
        Map<String, ReuseTempDTO> tempDTOMap = tradingTempStocks.stream().collect(Collectors.toMap(tempDTO -> tempDTO.getCode(), Function.identity(), (k1, k2) -> k1));

        List<Xnhk0603> lists = new LinkedList<>();
        Xnhks0101 xnhks0101 = xnhks0101Mapper.getStockShortSell(stockCode);
        if (ObjectUtil.isNotEmpty(xnhks0101) && StrUtil.equals("Y", xnhks0101.getF023v())) {
            ReuseTempDTO reuseTempDTO = tempDTOMap.get(stockCode);
            QueryWrapper<Xnhk0603> wrapper = new QueryWrapper<>();
            if (ObjectUtil.isNotEmpty(reuseTempDTO)&&null!=reuseTempDTO.getStartTime()){
                //若是临时股票则沽空数据需过滤掉当前并行交易前的数据
                wrapper.ge("F001D",DateUtils.formatDate(reuseTempDTO.getStartTime(),"yyyyMMdd"));
            }
            wrapper.eq("SECCODE", stockCode).orderByDesc("F001D").last("limit 60");
            lists = xnhk0603Mapper.selectList(wrapper);
//            if (CollectionUtils.isNotEmpty(lists)) {
//                StockSnapshot stockSnapshot = stockService.getOnlyStockSnapshot(stockCode);
//                if (ComStockStatueEnum.QUIT.getCode() == stockSnapshot.getSuspension()) {
//                    // 如果股票已退市，展示最后1个交易日数据
//                    lists = ListUtil.of(CollUtil.getFirst(lists));
//                }
//                Set<String> dayTimeSet = lists.stream().map(s -> DateUtil.format(DateUtil.parse(String.valueOf(s.getF001d()), DatePattern.PURE_DATE_FORMAT), DatePattern.NORM_DATETIME_FORMAT)).collect(Collectors.toSet());
//                if (CollUtil.isNotEmpty(dayTimeSet)) {
//                    List<KlineEntity> dailyKlineList = klineDailyApi.getTimeList(stockCode, new ArrayList<>(dayTimeSet));
//                    // 沽空收盘价取日K收盘价，10.7取10.7收盘价
//                    Map<Long, KlineEntity> dailyKlineMap = dailyKlineList.stream().collect(Collectors.toMap(k -> k.getTime(), v -> v, (o, v) -> v));
//                    Long lastTime = DateUtils.parseDate(lists.get(0).getF001d().toString()).getTime();
//                    ResultT<List<BgTradingCalendar>> lastTradingCalendars = hkTradingCalendarApi.getLastTradingCalendars(LocalDateTimeUtil.of(new Date(lastTime)).toLocalDate(), 60);
//                    if (ResultCode.SUCCESS.code()==lastTradingCalendars.getCode()) {
//                        Map<Long, Xnhk0603> xnhk0603Map = lists.stream().collect(Collectors.toMap(xnhk0603 -> DateUtils.parseDate(xnhk0603.getF001d().toString()).getTime(), Function.identity(), (k1, k2) -> k1));
//                        shortSales = lastTradingCalendars.getData().stream().map(tradingCalendar -> {
//                            Long time = DateUtils.localDate2Date(tradingCalendar.getDate()).getTime();
//                            Xnhk0603 xnhk0603 = xnhk0603Map.get(time);
//                            KlineEntity klineEntity = dailyKlineMap.get(time);
//                            return StockShortSale.builder()
//                                    .stockCode(ObjectUtil.isEmpty(xnhk0603)?null:xnhk0603.getSeccode())
//                                    .shortSaleRate(ObjectUtil.isEmpty(xnhk0603)?null:Optional.ofNullable(xnhk0603.getF005n()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
//                                    .marketShortSaleRate(ObjectUtil.isEmpty(xnhk0603)?null:xnhk0603.getF006n().setScale(2, RoundingMode.HALF_UP))
//                                    .shortSaleAvg(ObjectUtil.isEmpty(xnhk0603)?null:xnhk0603.getF004n().setScale(3, RoundingMode.HALF_UP))
//                                    // .close(Optional.ofNullable(s.getF007n()).orElse(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP))
//                                    .close(ObjectUtil.isEmpty(klineEntity) ? null:klineEntity.getClose())
//                                    .chgPct(ObjectUtil.isEmpty(klineEntity) ? null:klineEntity.getChgPct())
//                                    .shortSaleNum(ObjectUtil.isEmpty(xnhk0603)?null:xnhk0603.getF002n())
//                                    .time(time)
//                                    .build();
//                        }).collect(Collectors.toList());
//
//                    }
//
//
//                }
//            }
        }
        return lists;
    }

    /**
     * 沽空排行榜，条件：
     *      1. 股票支持沽空；
     *      2. 最近连续2个交易日有沽空记录，不包含今天；
     */
    public PageDomain<ComRankShortSale> getRankShortSale(Integer currentPage, Integer pageSize, String sort, String sortKey) {
        // 查询最近3个交易日，降序，包含今天
        List<BgTradingCalendar> tradingCalendars = hkTradingCalendarApi.getLastTradingCalendarsLtDate(LocalDate.now(), 2).getData();
        // 上个交易日
        LocalDate yesterday = CollUtil.getFirst(tradingCalendars).getDate();
        String key = String.format(RedisKeyConstants.RANK_SHORT_SALE_LIST, LocalDateTimeUtil.formatNormal(yesterday));
        List<ComRankShortSale> rankShortSales = redisClient.get(key);

        // 默认沽空量降序
        boolean isAsc = SortEnum.ASC.getValue().equals(sort);
        String order = StrUtil.blankToDefault(sortKey, "shortSaleNum");

        Comparator<ComRankShortSale> firstComp = Comparator.comparing(o -> {
            try {
                Object result =  ReflectUtil.getFieldValue(o, order);
                return ObjectUtil.isNotEmpty(result) ? new BigDecimal(result.toString()) : null;
            } catch (Exception e) {
                log.error("执行方法失败");
                return null;
            }
        }, Comparator.nullsFirst(BigDecimal::compareTo));
        Comparator<ComRankShortSale> secondComp = firstComp.thenComparing(ComRankShortSale::getStockCode);

        rankShortSales.sort(isAsc ? secondComp : secondComp.reversed());

        List<ComRankShortSale> pageRecords = ListUtil.page(currentPage - 1, pageSize, rankShortSales);
        if (CollUtil.isNotEmpty(pageRecords)) {
            List<String> codes = pageRecords.stream().map(o -> o.getStockCode()).collect(Collectors.toList());
            Map<String, Long> comStockSimpleDtoMap = stockCache.queryStockInfoList(codes).stream().collect(Collectors.toMap(o -> o.getCode(), o -> o.getStockId(), (k1, k2) -> k1));
            pageRecords.forEach(o->o.setStockId(comStockSimpleDtoMap.get(o.getStockCode())));
        }

        PageDomain<ComRankShortSale> result = new PageDomain<>();
        result.setCurrent(currentPage);
        result.setSize(pageSize);
        result.setTotal(CollUtil.size(rankShortSales));
        result.setRecords(pageRecords);

        return result;
    }

    public void saveDailyRankShortSale() {
        // 支持沽空的股票
        List<Xnhks0101> xnhks0101s = xnhks0101Mapper.selectList(Wrappers.<Xnhks0101>lambdaQuery().eq(Xnhks0101::getF023v, "Y"));
        List<String> xnhksCodes = xnhks0101s.stream().map(Xnhks0101::getSeccode).filter(code -> stockCache.queryStockNameMap(null).containsKey(code)).collect(Collectors.toList());
        // 查询前2个交易日，降序
        List<BgTradingCalendar> tradingCalendars = hkTradingCalendarApi.getLastTradingCalendarsLtDate(LocalDate.now(), 2).getData();
        // 上上个交易日
        LocalDate beforeYesterday = CollUtil.getLast(tradingCalendars).getDate();
        Long ymd = Long.valueOf(LocalDateTimeUtil.format(beforeYesterday, DatePattern.PURE_DATE_PATTERN));
        // 查询上个交易日拆并股
        List<Xnhk0127> xnhk0127s = utsInfoService.getXnhk0127(DateUtil.date(CollUtil.getFirst(tradingCalendars).getDate()));
        List<ComXnhk0127Dto> x0127DtoList = BeanUtil.copyToList(xnhk0127s, ComXnhk0127Dto.class);
        Map<String, List<ComXnhk0127Dto>> x0127CodeListMap = x0127DtoList.stream().collect(Collectors.groupingBy(ComXnhk0127Dto::getSeccode));

        // 查询沽空记录
        List<Xnhk0603> xnhk0603s = xnhk0603Mapper.selectList(Wrappers.<Xnhk0603>lambdaQuery().in(Xnhk0603::getSeccode, xnhksCodes).ge(Xnhk0603::getF001d, ymd).orderByAsc(Xnhk0603::getF001d));
        // 对code进行分组
        HashMap<String, List<Xnhk0603>> x0603CodeListMap = xnhk0603s.stream().collect(
                Collectors.groupingBy(Xnhk0603::getSeccode, HashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), list -> CollUtil.sort(list, Comparator.comparing(Xnhk0603::getF001d)))
                )
        );

        // 过滤，至少2个交易日有沽空 -> 聚合，取前2个交易日沽空数据 -> 分组
        Map<String, List<Xnhk0603>> filterGroupListMap = x0603CodeListMap.values().stream()
                .filter(list -> CollUtil.size(list) >= 2).map(list -> CollUtil.sub(list, 0, 2))
                .reduce(new ArrayList<>(), CollUtil::unionAll).stream().collect(Collectors.groupingBy(Xnhk0603::getSeccode));

        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        List<ComRankShortSale> rankShortSales = filterGroupListMap.keySet().stream().map(code -> {
            List<Xnhk0603> shortSales = filterGroupListMap.get(code);
            Xnhk0603 prev = CollUtil.getFirst(shortSales);
            Xnhk0603 next = CollUtil.getLast(shortSales);
            ComRankShortSale crs = new ComRankShortSale();
            crs.setStockCode(code);
            crs.setStockName(stockNameMap.get(code));
            crs.setShortSaleNum(next.getF002n());
            crs.setRegionType(RegionTypeEnum.HK.getCode());
            crs.setStockType(StockTypeEnum.STOCK.getCode());
            BigDecimal prevSellNum = BigDecimalUtil.null2Zero(prev.getF002n());
            if (x0127CodeListMap.containsKey(code)) {
                // 如果发生拆并股，对前日沽空量做复权处理
                prevSellNum = KlineCalcUtils.hkCalcForwardAmount(prevSellNum, x0127CodeListMap.get(code), false);
            }
            BigDecimal saleChangeRate = BigDecimalUtil.isNullOrZero(prevSellNum) ? BigDecimal.ZERO : NumberUtil.div(next.getF002n().subtract(prevSellNum), prevSellNum, 6);
            crs.setShortSaleChangeRate(saleChangeRate);
            return crs;
        }).collect(Collectors.toList());

        // 上个交易日
        LocalDate yesterday = CollUtil.getFirst(tradingCalendars).getDate();
        String key = String.format(RedisKeyConstants.RANK_SHORT_SALE_LIST, LocalDateTimeUtil.formatNormal(yesterday));
        redisClient.set(key, rankShortSales, 3600 * 24 * 7);
    }
}
