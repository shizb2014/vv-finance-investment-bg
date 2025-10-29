package com.vv.finance.investment.bg.api.impl.southward;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ibm.icu.text.Collator;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComScCapitalListResp;
import com.vv.finance.common.dto.ComScCapitalResp;
import com.vv.finance.common.dto.ComScNetInListResp;
import com.vv.finance.common.dto.ComScNetInTrendResp;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.CollectorsUtil;
import com.vv.finance.common.utils.SortListUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.southward.SouthwardCapitalApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.newcode.resp.NewStockListResp;
import com.vv.finance.investment.bg.dto.southward.req.SouthwardCapitalTrendReq;
import com.vv.finance.investment.bg.dto.southward.resp.CapitalSaveInfo;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalResp;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalStockDetailResp;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalTrendResp;
import com.vv.finance.investment.bg.dto.stock.SouthwardCapitalStockResp;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.southward.StockSouthwardCapitalStatistics;
import com.vv.finance.investment.bg.enums.SouthwardCapitalDateTypeEnum;
import com.vv.finance.investment.bg.enums.SouthwardCapitalMarketEnum;
import com.vv.finance.investment.bg.enums.SouthwardCapitalTrendTypeEnum;
import com.vv.finance.investment.bg.mapper.southward.StockSouthwardCapitalStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qinxi
 * @date 2023/6/25 15:13
 * @description: 南向资金api实现类
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class SouthwardCapitalApiImpl implements SouthwardCapitalApi {
    @Resource
    private RedisClient redisClient;

    @Resource
    private StockService stockService;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;


    @Autowired
    private StockCache stockCache;

    @Autowired
    private StockInfoApi stockInfoApi;

    @Resource
    private StockSouthwardCapitalStatisticsMapper stockSouthwardCapitalStatisticsMapper;

    private static final LocalTime NINE_HALF = LocalTime.of(9, 30);

    private static final int FIVE_DAY = 5;

    private static final int STOCK_LIMIT = 10;


    @Override
    public ResultT<List<StockCodeNameBaseDTO>> querySouthwardCapitalStockList(String sort, String sortKey) {
        log.info("查询南向资金详情列表相关的股票入参 sort:{} sortKey:{}", sort, sortKey);
        List<SouthwardCapitalStockDetailResp> detailRespList = this.querySouthwardCapitalStockDetail(null).getData();
        if (StrUtil.isNotBlank(sort) && StrUtil.isNotBlank(sortKey)) {
            if ("name".equals(sortKey)) {
                boolean ascFlag = SortListUtil.ASC.equalsIgnoreCase(sort);
                detailRespList.sort((o1, o2) -> {
                    Collator collator = Collator.getInstance(Locale.CHINESE);
                    return ascFlag ? collator.compare(o1.getName(), o2.getName()) : collator.compare(o2.getName(), o1.getName());
                });
            }else if ("code".equals(sortKey)) {
                boolean ascFlag = SortListUtil.ASC.equalsIgnoreCase(sort);
                Comparator<SouthwardCapitalStockDetailResp> comparing = Comparator.comparing(SouthwardCapitalStockDetailResp::getCode);
                detailRespList.sort(ascFlag ? comparing : comparing.reversed());
            } else {
                nullFirstSort(detailRespList, sortKey, sort);
            }
        }

        List<StockCodeNameBaseDTO> resultList = new ArrayList<>(detailRespList.size());
        for (SouthwardCapitalStockDetailResp detailResp : detailRespList) {
            StockCodeNameBaseDTO stockCodeNameBaseDTO = new StockCodeNameBaseDTO();
            stockCodeNameBaseDTO.setStockId(detailResp.getStockId());
            stockCodeNameBaseDTO.setCode(detailResp.getCode());
            stockCodeNameBaseDTO.setName(detailResp.getName());
            stockCodeNameBaseDTO.setStockType(detailResp.getStockType());
            stockCodeNameBaseDTO.setRegionType(detailResp.getRegionType());
            resultList.add(stockCodeNameBaseDTO);
        }
        log.info("查询南向资金详情列表相关的股票出参 resultList:{}", resultList);
        return ResultT.success(resultList);
    }

    /**
     * 排序，空字段升序排最前
     */
    private void nullFirstSort(List<SouthwardCapitalStockDetailResp> list, String sortKey, String sort){
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, SouthwardCapitalStockDetailResp.class);
            Method readMethod = descriptor.getReadMethod();
            list.sort((t1, t2) -> {
                try {
                    Object result1 = readMethod.invoke(t1);
                    BigDecimal decimal1 = result1 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result1.toString());
                    Object result2 = readMethod.invoke(t2);
                    BigDecimal decimal2 = result2 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result2.toString());
                    if (SortListUtil.ASC.equalsIgnoreCase(sort)) {
                        return decimal1.compareTo(decimal2);
                    } else {
                        return decimal2.compareTo(decimal1);
                    }
                } catch (Exception e) {
                    log.error("执行排序方法失败",e);
                    return 0;
                }
            });
        } catch (Exception e) {
            log.error("排序失败", e);
        }
    }

    @Override
    public ResultT<List<SouthwardCapitalStockDetailResp>> querySouthwardCapitalStockDetail(List<String> stockCodeList) {
        log.info("查询南向资金详情列表入参 req:{}", JSONUtil.toJsonStr(stockCodeList));
        Map<String, SouthwardCapitalStockDetailResp> stockDetailMap = redisClient.hmget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP);
        if (CollUtil.isEmpty(stockCodeList)) {
            stockCodeList = new ArrayList<>(stockDetailMap.keySet());
        }
        Map<String, StockSnapshot> stockSnapshotMap = stockService.getSnapshotList(stockCodeList.toArray(new String[0])).stream().collect(Collectors.toMap(StockSnapshot::getCode, Function.identity(), (v1, v2) -> v1));
        List<SouthwardCapitalStockDetailResp> stockDetailList = new ArrayList<>(stockCodeList.size());
        for (String stockCode : stockCodeList) {
            SouthwardCapitalStockDetailResp detailResp = stockDetailMap.get(stockCode);
            if (detailResp == null) {
                continue;
            }
            StockSnapshot stockSnapshot = stockSnapshotMap.get(stockCode);
            if (stockSnapshot != null) {
                //股票快照
                detailResp.setStockId(stockSnapshot.getStockId());
                detailResp.setName(stockSnapshot.getName());
                detailResp.setChgPct(stockSnapshot.getChgPct());
                detailResp.setChg(stockSnapshot.getChg());
                detailResp.setLast(stockSnapshot.getLast());
            } else {
                log.info("不存在该股票的快照:{}", stockCode);
            }
            stockDetailList.add(detailResp);
        }
        stockDetailList.sort(Comparator.comparing(SouthwardCapitalStockDetailResp::getHoldingMarkValue).reversed());
        log.info("查询南向资金详情列表出参 list:{}", JSONUtil.toJsonStr(stockDetailList));
        return ResultT.success(stockDetailList);
    }

    @Override
    public ResultT<List<SouthwardCapitalResp>> querySouthwardCapitalList() {
        List<SouthwardCapitalResp> list = new ArrayList<>();
        Map<String, List<SouthwardCapitalStatistics>> map = redisClient.hmget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_LIST_MAP);
        if (CollUtil.isEmpty(map)) {
            return ResultT.success(list);
        }
        log.info("SouthwardCapitalList from redis：{}", JSONUtil.toJsonStr(map));
        for (String market : map.keySet()) {
            SouthwardCapitalResp southwardCapitalResp = new SouthwardCapitalResp();
            southwardCapitalResp.setMarket(market);
            List<SouthwardCapitalStatistics> southwardCapitalStatistics = map.get(market);
            if (CollUtil.isEmpty(southwardCapitalStatistics)) {
                continue;
            }
            southwardCapitalResp.setNetBuyingTurnover(this.getSumValue(southwardCapitalStatistics, 1, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetTurnoverIn(this.getSumValue(southwardCapitalStatistics, 1, SouthwardCapitalStatistics::getNetTurnoverIn));
            southwardCapitalResp.setSurplusQuota(this.getSumValue(southwardCapitalStatistics, 1, SouthwardCapitalStatistics::getSurplusQuota));

            southwardCapitalResp.setNetBuyingTurnoverNearly5Days(this.getSumValue(southwardCapitalStatistics, 5, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetTurnoverInNearly5Days(this.getSumValue(southwardCapitalStatistics, 5, SouthwardCapitalStatistics::getNetTurnoverIn));

            southwardCapitalResp.setNetBuyingTurnoverNearly20Days(this.getSumValue(southwardCapitalStatistics, 20, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetTurnoverInNearly20Days(this.getSumValue(southwardCapitalStatistics, 20, SouthwardCapitalStatistics::getNetTurnoverIn));

            southwardCapitalResp.setNetBuyingTurnoverNearly60Days(this.getSumValue(southwardCapitalStatistics, 60, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetTurnoverInNearly60Days(this.getSumValue(southwardCapitalStatistics, 60, SouthwardCapitalStatistics::getNetTurnoverIn));
            list.add(southwardCapitalResp);
        }
        log.info("南向资金列表:{}", JSONUtil.toJsonStr(list));
        return ResultT.success(list);
    }

    @Override
    public ResultT<List<ComScCapitalResp>> querySouthwardCapitalList2() {
        List<SouthwardCapitalResp> list = new ArrayList<>();
        Map<String, List<SouthwardCapitalStatistics>> map = redisClient.hmget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_LIST_MAP);
        if (CollUtil.isEmpty(map)) {
            return ResultT.success();
        }
        log.info("SouthwardCapitalList from redis：{}", JSONUtil.toJsonStr(map));
        for (String market : map.keySet()) {
            SouthwardCapitalResp southwardCapitalResp = new SouthwardCapitalResp();
            southwardCapitalResp.setMarket(market);
            List<SouthwardCapitalStatistics> southwardCapitalStatistics = map.get(market);
            if (CollUtil.isEmpty(southwardCapitalStatistics)) {
                continue;
            }
            southwardCapitalResp.setNetBuyingTurnover(this.getSumValue(southwardCapitalStatistics, 1, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetBuyingTurnoverNearly5Days(this.getSumValue(southwardCapitalStatistics, 5, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetBuyingTurnoverNearly20Days(this.getSumValue(southwardCapitalStatistics, 20, SouthwardCapitalStatistics::getNetBuyingTurnover));
            southwardCapitalResp.setNetBuyingTurnoverNearly60Days(this.getSumValue(southwardCapitalStatistics, 60, SouthwardCapitalStatistics::getNetBuyingTurnover));
            list.add(southwardCapitalResp);
        }

        // 沪港通、深港通、南向资金
        SouthwardCapitalResp shCapResp = CollUtil.findOne(list, resp -> StrUtil.equalsIgnoreCase(SouthwardCapitalMarketEnum.SH.getMarket(), resp.getMarket()));
        SouthwardCapitalResp szCapResp = CollUtil.findOne(list, resp -> StrUtil.equalsIgnoreCase(SouthwardCapitalMarketEnum.SZ.getMarket(), resp.getMarket()));
        SouthwardCapitalResp allCapResp = CollUtil.findOne(list, resp -> StrUtil.equalsIgnoreCase(SouthwardCapitalMarketEnum.ALL.getMarket(), resp.getMarket()));

        ComScCapitalResp todayDetail = buildCapitalDetail(shCapResp, szCapResp, allCapResp, SouthwardCapitalResp::getNetBuyingTurnover);
        ComScCapitalResp fiveDayDetail = buildCapitalDetail(shCapResp, szCapResp, allCapResp, SouthwardCapitalResp::getNetBuyingTurnoverNearly5Days);
        ComScCapitalResp twentyDayDetail = buildCapitalDetail(shCapResp, szCapResp, allCapResp, SouthwardCapitalResp::getNetBuyingTurnoverNearly20Days);
        ComScCapitalResp sixtyDayDetail = buildCapitalDetail(shCapResp, szCapResp, allCapResp, SouthwardCapitalResp::getNetBuyingTurnoverNearly60Days);

        List<ComScCapitalResp> details = ListUtil.of(todayDetail, fiveDayDetail, twentyDayDetail, sixtyDayDetail);
        log.info("南向资金列表:{}", JSONUtil.toJsonStr(details));
        return ResultT.success(details);
    }

    private ComScCapitalResp buildCapitalDetail(SouthwardCapitalResp shCapResp, SouthwardCapitalResp szCapResp, SouthwardCapitalResp allCapResp, Function<SouthwardCapitalResp, BigDecimal> function) {
        ComScCapitalResp capitalDetail = new ComScCapitalResp();
        capitalDetail.setShNetBuyingTurnover(function.apply(shCapResp));
        capitalDetail.setSzNetBuyingTurnover(function.apply(szCapResp));
        capitalDetail.setSouthwardTurnover(function.apply(allCapResp));
        return capitalDetail;
    }

    private BigDecimal getSumValue(List<SouthwardCapitalStatistics> list, int limit, Function<SouthwardCapitalStatistics, BigDecimal> fun) {
        return list.stream()
                .limit(limit)
                .filter(item -> Objects.nonNull(fun.apply(item)))
                .collect(CollectorsUtil.summingBigDecimal(fun));
    }


    @Override
    public ResultT<SouthwardCapitalTrendResp> querySouthwardCapitalTrend(SouthwardCapitalTrendReq req) {
        log.info("查询南向资金趋势图,req={}", JSONUtil.toJsonStr(req));
        SouthwardCapitalTrendResp resp = getCapitalTrendList(req);
        log.info("查询南向资金趋势图,resp={}", JSONUtil.toJsonStr(resp));
        return ResultT.success(resp);
    }

    @Override
    public ResultT<List<SouthwardCapitalStockResp>> querySouthwardCapitalStock(String stockCode) {
        List<SouthwardCapitalStockResp> list = stockCache.getStockSimpleInfoAll(stockCode).stream().map(stockInfo -> {
            SouthwardCapitalStockResp resp = new SouthwardCapitalStockResp();
            BeanUtils.copyProperties(stockInfo, resp);
            return resp;
        }).collect(Collectors.toList());
        ResultT<List<SouthwardCapitalStockDetailResp>> result = this.querySouthwardCapitalStockDetail(null);
        List<String> hkStockList = result.getData().stream().map(SouthwardCapitalStockDetailResp::getCode).collect(Collectors.toList());
        list = list.stream().filter(s -> s.getType() != 2 && hkStockList.contains(s.getCode())).collect(Collectors.toList());
        if (!CollectionUtil.isEmpty(list) && list.size() > STOCK_LIMIT) {
            return ResultT.success(list.subList(0, STOCK_LIMIT));
        }
        return ResultT.success(list);
    }

    private SouthwardCapitalTrendResp getCapitalTrendList(SouthwardCapitalTrendReq req) {
        Integer trendType = req.getTrendType();
        SouthwardCapitalDateTypeEnum dateTypeEnum = SouthwardCapitalDateTypeEnum.getByDateType(req.getDateType());
        List<CapitalSaveInfo> capitalList = getCapitalList(dateTypeEnum);

        SouthwardCapitalTrendResp resp = new SouthwardCapitalTrendResp();
        if (CollectionUtils.isEmpty(capitalList)) {
            resp.setCapitalTrendList(new ArrayList<>());
            return resp;
        }
        resp.setCapitalTrendList(getTrendList(trendType, capitalList));
        return resp;
    }

    private List<SouthwardCapitalTrendResp.CapitalTrend> getTrendList(Integer trendType, List<CapitalSaveInfo> capitalList) {
        if (trendType.equals(SouthwardCapitalTrendTypeEnum.NET_BUYING_TURNOVER.getTrendType())) {
            return getTrendListForNetBuyingTurnover(capitalList);
        }
        if (trendType.equals(SouthwardCapitalTrendTypeEnum.NET_TURNOVER_IN.getTrendType())) {
            return getTrendListForNetTurnoverIn(capitalList);
        }
        if (trendType.equals(SouthwardCapitalTrendTypeEnum.SURPLUS_QUOTA.getTrendType())) {
            return getTrendListForSurplusQuota(capitalList);
        }
        return new ArrayList<>();
    }

    private List<CapitalSaveInfo> getCapitalList(SouthwardCapitalDateTypeEnum dateTypeEnum) {
        LocalDate currentDate = LocalDate.now();
        if (dateTypeEnum == SouthwardCapitalDateTypeEnum.TODAY) {
            boolean tradingDay = hkTradingCalendarApi.isTradingDay(currentDate);
            if (!tradingDay || LocalTime.now().isBefore(NINE_HALF)) {
                //非交易日或者交易日9:30:00之前获取上一个交易日数据
                LocalDate lastTradingDate = hkTradingCalendarApi.getBeforeTradingCalendar(currentDate).getDate();
                return redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, lastTradingDate));
            } else {
                return redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, currentDate));
            }
        }

        if (dateTypeEnum == SouthwardCapitalDateTypeEnum.NEARLY5DAYS) {
            return getFiveDayCapitalSaveInfo();
        }
        List<CapitalSaveInfo> capitalList = getSixtyDayCapitalSaveInfo();

        if (dateTypeEnum == SouthwardCapitalDateTypeEnum.NEARLY60DAYS) {
            return capitalList;
        }

        if (dateTypeEnum == SouthwardCapitalDateTypeEnum.NEARLY20DAYS) {
            if (!CollectionUtils.isEmpty(capitalList) && capitalList.size() > 20) {
                capitalList.sort(Comparator.comparing(CapitalSaveInfo::getTime));
                return capitalList.subList(capitalList.size() - 20, capitalList.size());
            } else {
                return capitalList;
            }
        }
        return new ArrayList<>();
    }

    private List<CapitalSaveInfo> getFiveDayCapitalSaveInfo() {
        List<CapitalSaveInfo> capitalList = new ArrayList<>();
        List<BgTradingCalendar> fiveList = getFiveDayTradingDate();
        for (BgTradingCalendar calendar : fiveList) {
            LocalDate tradingDate = calendar.getDate();
            List<CapitalSaveInfo> list = redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, tradingDate));
            if (!CollectionUtil.isEmpty(list)) {
                capitalList.addAll(list);
            }
        }
        return capitalList;
    }


    private List<CapitalSaveInfo> getSixtyDayCapitalSaveInfo() {
        List<CapitalSaveInfo> capitalList = redisClient.get(RedisKeyConstants.SOUTHWARD_CAPITAL_SIXTY);
        return capitalList;
    }

    /**
     * 获取五日交易日期
     */
    private List<BgTradingCalendar> getFiveDayTradingDate() {
        List<BgTradingCalendar> list;
        if (!hkTradingCalendarApi.isTradingDay(LocalDate.now()) || LocalTime.now().isBefore(NINE_HALF)) {
            list = hkTradingCalendarApi.getLastTradingCalendars(LocalDate.now().plusDays(-1), FIVE_DAY).getData();
        } else {
            list = hkTradingCalendarApi.getLastTradingCalendars(LocalDate.now(), FIVE_DAY).getData();
        }
        Collections.sort(list, Comparator.comparing(BgTradingCalendar::getDate));
        return list;
    }

    private List<SouthwardCapitalTrendResp.CapitalTrend> getTrendListForNetBuyingTurnover(List<CapitalSaveInfo> capitalList) {
        List<SouthwardCapitalTrendResp.CapitalTrend> netBuyingTurnoverList = new ArrayList();
        capitalList.stream().forEach(capital -> {
            SouthwardCapitalTrendResp.CapitalTrend netBuyingTurnover = new SouthwardCapitalTrendResp.CapitalTrend();
            netBuyingTurnover.setTime(capital.getTime());
            netBuyingTurnover.setShCapital(capital.getShNetBuyingTurnover());
            netBuyingTurnover.setSzCapital(capital.getSzNetBuyingTurnover());
            netBuyingTurnover.setAllCapital(capital.getAllNetBuyingTurnover());
            netBuyingTurnover.setHengShengIndex(capital.getHengShengIndex());
            netBuyingTurnoverList.add(netBuyingTurnover);
        });
        return netBuyingTurnoverList;
    }

    private List<SouthwardCapitalTrendResp.CapitalTrend> getTrendListForNetTurnoverIn(List<CapitalSaveInfo> capitalList) {
        List<SouthwardCapitalTrendResp.CapitalTrend> netTurnoverInList = new ArrayList();
        capitalList.stream().forEach(capital -> {
            SouthwardCapitalTrendResp.CapitalTrend netTurnoverIn = new SouthwardCapitalTrendResp.CapitalTrend();
            netTurnoverIn.setTime(capital.getTime());
            netTurnoverIn.setShCapital(capital.getShNetTurnoverIn());
            netTurnoverIn.setSzCapital(capital.getSzNetTurnoverIn());
            netTurnoverIn.setAllCapital(capital.getAllNetTurnoverIn());
            netTurnoverIn.setHengShengIndex(capital.getHengShengIndex());
            netTurnoverInList.add(netTurnoverIn);
        });
        return netTurnoverInList;
    }

    private List<SouthwardCapitalTrendResp.CapitalTrend> getTrendListForSurplusQuota(List<CapitalSaveInfo> capitalList) {
        List<SouthwardCapitalTrendResp.CapitalTrend> surplusQuotaList = new ArrayList();
        capitalList.stream().forEach(capital -> {
            SouthwardCapitalTrendResp.CapitalTrend surplusQuota = new SouthwardCapitalTrendResp.CapitalTrend();
            surplusQuota.setTime(capital.getTime());
            surplusQuota.setShCapital(capital.getShSurplusQuota());
            surplusQuota.setSzCapital(capital.getSzSurplusQuota());
            surplusQuota.setAllCapital(capital.getAllSurplusQuota());
            surplusQuota.setHengShengIndex(capital.getHengShengIndex());
            surplusQuotaList.add(surplusQuota);
        });
        return surplusQuotaList;
    }

    @Override
    public ResultT<ComScNetInTrendResp> querySouthwardCapitalNetInTrend(Long stockId) {
        LocalDate localDate = LocalDate.now();
        // 1. 非交易日显示上个交易日的数据
        // 2. 交易日期9:30之前，展示上上个交易日数据
        // 3. 交易日9:30之后，展示上个交易日数据
        if (hkTradingCalendarApi.isTradingDay(LocalDate.now()) && LocalTime.now().isBefore(LocalTime.of(9, 30))) {
            localDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        }

        ResultT<List<BgTradingCalendar>> lastTradingCalendars = hkTradingCalendarApi.getLastTradingCalendarsLtDate(localDate, 60);
        List<LocalDate> dateList = lastTradingCalendars.getData().stream().map(trade -> trade.getDate()).collect(Collectors.toList());

        // 查询近60个交易日
        QueryWrapper<StockSouthwardCapitalStatistics> queryWrapper = new QueryWrapper<StockSouthwardCapitalStatistics>().eq("stock_id", stockId).lt("statistics_date", localDate).orderByDesc("statistics_date").last("limit 80");
        List<StockSouthwardCapitalStatistics> capitalStatistics = stockSouthwardCapitalStatisticsMapper.selectList(queryWrapper);
        Map<LocalDate, StockSouthwardCapitalStatistics> dateCapitalStatisticsMap = CollUtil.toMap(capitalStatistics, new HashMap<>(), StockSouthwardCapitalStatistics::getStatisticsDate, v -> v);

        List<ComScNetInTrendResp.ComScNetInTrend> comScNetInTrends = dateList.stream().map(date -> {
            ComScNetInTrendResp.ComScNetInTrend comScNetInTrend = new ComScNetInTrendResp.ComScNetInTrend();
            comScNetInTrend.setTime(LocalDateTimeUtil.toEpochMilli(date));
            StockSouthwardCapitalStatistics statistics = dateCapitalStatisticsMap.get(date);
            comScNetInTrend.setNetIn(ObjectUtil.isNotEmpty(statistics) ? statistics.getTodayNetTurnoverIn() : BigDecimal.ZERO);
            return comScNetInTrend;
        }).collect(Collectors.toList());

        ComScNetInTrendResp netInTrendResp = ComScNetInTrendResp.builder().stockId(stockId).trendList(comScNetInTrends).build();
        return ResultT.success(netInTrendResp);
    }

    @Override
    public ResultT<ComScNetInListResp> querySouthwardCapitalNetInList(Long stockId) {
        log.info("查询南向资金详情列表入参 stockId:{}", stockId);
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockDtoList(Collections.singletonList(stockId));
        ComStockSimpleDto simpleDto = CollUtil.getFirst(simpleDtoList);
        SouthwardCapitalStockDetailResp detailResp = redisClient.hget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, simpleDto.getCode());
        ComScNetInListResp netInListResp = ComScNetInListResp.builder().stockId(stockId).detailList(new ArrayList<>()).build();

        if (ObjectUtil.isNotEmpty(detailResp)) {
            // 昨日
            ComScNetInListResp.ComScNetInDetail lastDayDetail = ComScNetInListResp.ComScNetInDetail.builder()
                    .netBuyingShares(detailResp.getNetBuyingShares()).netTurnoverIn(detailResp.getNetTurnoverIn()).turnoverRank(getTurnoverRank(detailResp.getTurnoverRank(), detailResp.getTotalRank())).build();
            // 前5日
            ComScNetInListResp.ComScNetInDetail fiveDayDetail = ComScNetInListResp.ComScNetInDetail.builder()
                    .netBuyingShares(detailResp.getNetBuyingSharesNearly5Days()).netTurnoverIn(detailResp.getNetTurnoverInNearly5Days()).turnoverRank(getTurnoverRank(detailResp.getTurnoverRankNearly5Days(), detailResp.getTotalRank())).build();
            // 前20日
            ComScNetInListResp.ComScNetInDetail twentyDayDetail = ComScNetInListResp.ComScNetInDetail.builder()
                    .netBuyingShares(detailResp.getNetBuyingSharesNearly20Days()).netTurnoverIn(detailResp.getNetTurnoverInNearly20Days()).turnoverRank(getTurnoverRank(detailResp.getTurnoverRankNearly20Days(),detailResp.getTotalRank())).build();
            // 前60日
            ComScNetInListResp.ComScNetInDetail sixtyDayDetail = ComScNetInListResp.ComScNetInDetail.builder()
                    .netBuyingShares(detailResp.getNetBuyingSharesNearly60Days()).netTurnoverIn(detailResp.getNetTurnoverInNearly60Days()).turnoverRank(getTurnoverRank(detailResp.getTurnoverRankNearly60Days(),detailResp.getTotalRank())).build();
            List<ComScNetInListResp.ComScNetInDetail> detailList = ListUtil.of(lastDayDetail, fiveDayDetail, twentyDayDetail, sixtyDayDetail);
            netInListResp.setDetailList(detailList);
        }

        log.info("查询南向资金详情列表出参 stockId:{}", stockId);
        return ResultT.success(netInListResp);
    }

    private String getTurnoverRank(Integer dayRank, Integer totalRank) {
        if(ObjectUtils.isEmpty(dayRank)  || ObjectUtils.isEmpty(totalRank)){
            return "";
        }
        return dayRank + "/" + totalRank;
    }

    @Override
    public void createSouthwardDataByCode(String stockCode) {
        try {
            log.info("SouthwardCapitalApi createSouthwardDataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();

            String oldCode = StrUtil.replace(stockCode, "-t", "");
            Map<String, Long> stockIdMap = stockCache.queryStockIdMap(ListUtil.of(stockCode, oldCode));

            if (MapUtil.isEmpty(stockIdMap) || !stockIdMap.containsKey(stockCode) || !stockIdMap.containsKey(oldCode)) {
                log.info("SouthwardCapitalApi createSouthwardDataByCode queryStockIdMap failed, stockCode: {}, stockIdMap:{}", stockCode, stockIdMap);
                return;
            }

            LambdaQueryWrapper<StockSouthwardCapitalStatistics> wrapper = Wrappers.<StockSouthwardCapitalStatistics>lambdaQuery().eq(StockSouthwardCapitalStatistics::getStockId, stockIdMap.get(oldCode));
            List<StockSouthwardCapitalStatistics> brokerStatistics = stockSouthwardCapitalStatisticsMapper.selectList(wrapper);

            CollUtil.forEach(brokerStatistics, (ff, index) -> { ff.setStockId(stockIdMap.get(stockCode)); ff.setCode(stockCode); });
            Opt.ofEmptyAble(brokerStatistics).peek(list -> stockSouthwardCapitalStatisticsMapper.saveOrUpdateBatch(list));

            SouthwardCapitalStockDetailResp detailResp = redisClient.hget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, oldCode);
            if (ObjectUtil.isNotEmpty(detailResp)) {
                detailResp.setCode(stockCode);
                detailResp.setStockId(stockIdMap.get(stockCode));
                redisClient.hset(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, stockCode, detailResp, (long) 60 * 60 * 24 * 10);
            }
            log.info("SouthwardCapitalApi createSouthwardDataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("SouthwardCapitalApi createSouthwardDataByCode error, stockCode: {}", stockCode, e);
        }
    }

    @Override
    public void deleteSouthwardDataByCode(String stockCode) {
        try {
            log.info("SouthwardCapitalApi deleteSouthwardDataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();
            
            // 所有经纪商数据
            LambdaQueryWrapper<StockSouthwardCapitalStatistics> wrapper = Wrappers.<StockSouthwardCapitalStatistics>lambdaQuery().eq(StockSouthwardCapitalStatistics::getCode, stockCode);
            stockSouthwardCapitalStatisticsMapper.delete(wrapper);

            redisClient.hdel(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, stockCode);

            log.info("SouthwardCapitalApi deleteSouthwardDataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("SouthwardCapitalApi deleteSouthwardDataByCode error, stockCode: {}", stockCode, e);
        }
    }

    @Override
    public void updateSouthwardDataByCode(String oldStockCode, String newStockCode) {
        try {
            log.info("SouthwardCapitalApi updateSouthwardDataByCode start, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode);
            TimeInterval timeInterval = new TimeInterval();
            Map<String, Long> stockIdMap = stockCache.queryStockIdMap(ListUtil.of(oldStockCode, newStockCode));

            if (MapUtil.isEmpty(stockIdMap) && (!stockIdMap.containsKey(oldStockCode) || !stockIdMap.containsKey(newStockCode))) {
                log.info("SouthwardCapitalApi createSouthwardDataByCode queryStockIdMap failed, oldStockCode: {}, stockIdMap:{}", oldStockCode, stockIdMap);
                return;
            }

            // 股票转板，stockId不变
            LambdaUpdateWrapper<StockSouthwardCapitalStatistics> updateWrapper = Wrappers.<StockSouthwardCapitalStatistics>lambdaUpdate()
                    .set(StockSouthwardCapitalStatistics::getCode, newStockCode)
                    .eq(StockSouthwardCapitalStatistics::getStockId, stockIdMap.get(newStockCode));
            stockSouthwardCapitalStatisticsMapper.update(null, updateWrapper);

            SouthwardCapitalStockDetailResp detailResp = redisClient.hget(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, oldStockCode);
            if (ObjectUtil.isNotEmpty(detailResp)) {
                detailResp.setCode(newStockCode);
                detailResp.setStockId(stockIdMap.get(newStockCode));
                redisClient.hdel(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, oldStockCode);
                redisClient.hset(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, newStockCode, detailResp, (long) 60 * 60 * 24 * 10);
            }
            log.info("SouthwardCapitalApi updateSouthwardDataByCode end, oldStockCode: {}, newStockCode: {}, cost: {}", oldStockCode, newStockCode, timeInterval.interval() / 1000.0);
        } catch (NumberFormatException e) {
            log.error("SouthwardCapitalApi updateSouthwardDataByCode error, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode, e);
        }
    }
}
