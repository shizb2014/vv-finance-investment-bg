package com.vv.finance.investment.bg.job.southward;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.frontend.v2.StockServiceV2;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.dto.southward.resp.CapitalSaveInfo;
import com.vv.finance.investment.bg.dto.southward.resp.HengShengIndexInfo;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalStatisticsDTO;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalStockDetailResp;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.TargetJobBean;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.southward.StockSouthwardCapitalStatistics;
import com.vv.finance.investment.bg.enums.SouthwardCapitalMarketEnum;
import com.vv.finance.investment.bg.mapper.southward.SouthwardCapitalStatisticsMapper;
import com.vv.finance.investment.bg.mapper.southward.StockSouthwardCapitalStatisticsMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.utils.DingUtil;
import com.vv.finance.investment.gateway.api.stock.HkStockThroughApi;
import com.vv.finance.investment.gateway.api.stock.IHsStockBusinessApi;
import com.vv.finance.investment.gateway.dto.ShareholdingRatioInfoDTO;
import com.vv.finance.investment.gateway.dto.req.ConnectBalanceReqDTO;
import com.vv.finance.investment.gateway.dto.req.TurnoverFlowReqDTO;
import com.vv.finance.investment.gateway.dto.resp.ConnectBalanceRespDTO;
import com.vv.finance.investment.gateway.dto.resp.NetBuyingTurnoverResp;
import com.vv.finance.investment.gateway.dto.resp.NetTurnoverInResp;
import com.vv.finance.investment.gateway.dto.resp.TurnoverFlowRespDTO;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qinxi
 * @date 2023/6/28 10:49
 * @description: 南向资金job
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SouthwardCapitalJob {

    @Resource
    private StockSouthwardCapitalStatisticsMapper stockSouthwardCapitalStatisticsMapper;

    @Resource
    private Xnhks0101Mapper xnhks0101Mapper;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice",timeout = 12000)
    private HkStockThroughApi hkStockThroughApi;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private IHsStockBusinessApi iHsStockBusinessApi;

    @Resource
    private StockService stockService;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    @Resource
    private RedisClient redisClient;

    @Resource
    private IStockKlineService iStockKlineService;

    @Resource
    private StockServiceV2 stockServiceV2;

    @Resource
    private SouthwardCapitalStatisticsMapper southwardCapitalStatisticsMapper;

    @Resource
    private StockCache stockCache;

    private static final String INDEX_CODE = "0000100";

    private static final String ADJHKT_FORWARD = "forward";

    private static final String KLINE_TYPE_DAY = "day";

    private static final String ADJHKT_NOT = "not";

    private static final String KLINE_TYPE_RT = "rt";

    private static final String KLINE_TYPE_MIN = "min1";

    private static final LocalTime NINE_HALF = LocalTime.of(9, 30);

    private static final String HANDLE_AM = "am";
    private static final String HANDLE_PM = "pm";
    private static final String HANDLE_ALL = "all";
    private static final String HANDLE_TYPE_ZERO = "zero";
    private static final String HANDLE_TYPE_MINUTE = "minute";

    private static final String HANDLE_FIELD_ALL = "all";
    private static final String HANDLE_FIELD_TURNOVER_FLOW = "flow";
    private static final String HANDLE_FIELD_TURNOVER_IN = "in";



    private static final String CERTAIN_DAY = "2023-10-09";


    @Value("${southward.minute.limit:331}")
    private Integer southwardMinuteLimit;

    @XxlJob(value = "saveStockSouthwardCapitalJob", author = "秦禧", desc = "将历史的个股南向资金落库 入参:股票code 0表示所有股票,近几个交易日 例(00700.hk,10)")
    public ReturnT<String> saveStockSouthwardCapitalJob(String param) {
        MDC.put("TraceId", IdUtil.simpleUUID());
        log.info("将历史的南向资金落库启动, param:{}", param);
        TargetJobBean jobBean = StrUtil.isBlank(param) ? new TargetJobBean() : JSON.parseObject(param, TargetJobBean.class);
        long t = System.currentTimeMillis();
        // LocalDate today = LocalDate.now();
        // String[] split = param.split(",");
        // String stockCode = Objects.equals(split[0], "0") ? null : split[0];
        int number = ObjectUtil.isNotEmpty(jobBean.getNum()) ? jobBean.getNum() : 1;
        String date = StrUtil.isNotBlank(jobBean.getDate()) ? jobBean.getDate() : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> stockCodeList = this.getHkStockThroughList(jobBean.getCodes());
        List<StockSouthwardCapitalStatistics> southwardCapitalStatistics = new ArrayList<>();
        //是否分批保存
        // boolean partialStorage = stockCode == null && number >= 1000000;
        // 股票id关系
        Map<String, ComStockSimpleDto> stockInfoMap = stockCache.queryStockInfoMap(stockCodeList);
        //获取股票快照
        Map<String, StockSnapshot> stockSnapshotMap = stockService.getSnapshotList(stockCodeList.toArray(new String[0])).stream().collect(Collectors.toMap(StockSnapshot::getCode, Function.identity(), (v1, v2) -> v1));
        for (String code : stockCodeList) {
            StockSnapshot stockSnapshot = stockSnapshotMap.get(code);
            if (stockSnapshot == null) {
                log.info("不存在该股票的快照：{}", code);
                continue;
            }
            if (!stockInfoMap.containsKey(code)) {
                log.info("不存在该股票的stockId：{}", code);
                continue;
            }
            long start = System.currentTimeMillis();
            ResultT<ShareholdingRatioInfoDTO> resultT = hkStockThroughApi.getShareholdingRatioInfo(date, number, code);
            log.info("数量[{}]日期[{}]股票[{}]南向资金数据查询耗时：{}ms", number, date, code, System.currentTimeMillis() - start);
            if (resultT.getCode() != ResultCode.SUCCESS.code()) {
                log.warn("数量[{}]日期[{}]股票[{}]南向资金数据查询失败", number, date, code);
                continue;
            }
            List<ShareholdingRatioInfoDTO.InfoItem> infoItemList = resultT.getData().getInfoItemList();
            if (CollectionUtil.isEmpty(infoItemList)) {
                log.info("数量[{}]日期[{}]股票[{}]南向资金数据查询结果为空", number, date, code);
                continue;
            }
            ComStockSimpleDto simpleDto = stockInfoMap.get(code);
            for (ShareholdingRatioInfoDTO.InfoItem infoItem : infoItemList) {
                StockSouthwardCapitalStatistics stockSouthward = new StockSouthwardCapitalStatistics();
                stockSouthward.setCode(code);
                stockSouthward.setStockId(simpleDto.getStockId());
                stockSouthward.setName(simpleDto.getStockName());
                stockSouthward.setStatisticsDate(infoItem.getDate());
                stockSouthward.setCreateTime(LocalDateTime.now());
                stockSouthward.setUpdateTime(LocalDateTime.now());
                stockSouthward.setTodayNetTurnoverIn(infoItem.getNetTurnoverIn());
                stockSouthward.setTodayNetBuyingShares(infoItem.getNetBuyingShares());
                //持股比例
                stockSouthward.setTodayHoldingRate(BigDecimalUtil.divideSaveSix(infoItem.getShareholdingsRatio(), BigDecimal.valueOf(100)));
                //持股市值=昨收价*昨日持股数量
                stockSouthward.setTodayHoldingMarketValue(NumberUtil.mul(stockSnapshot.getPreClose(), infoItem.getShareholdings()));
                southwardCapitalStatistics.add(stockSouthward);
            }
        }
        if (CollUtil.isNotEmpty(southwardCapitalStatistics)) {
            List<List<StockSouthwardCapitalStatistics>> partitions = ListUtil.partition(southwardCapitalStatistics, 1000);
            partitions.forEach(part -> stockSouthwardCapitalStatisticsMapper.saveOrUpdateBatch(part));
            log.info("开始全量存db, size:{}", southwardCapitalStatistics.size());
        }
        //总耗时:落地所有港股通股票的历史数据需207639ms => 3.5分钟
        log.info("将历史的南向资金落库结束, 总耗时:{}ms", System.currentTimeMillis() - t);
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "saveSouthwardCapitalJob", author = "秦禧", desc = "将南向资金落库 入参:近几个交易日")
    public ReturnT<String> saveSouthwardCapitalJob(String param) {
        MDC.put("TraceId", IdUtil.simpleUUID());
        log.info("将南向资金落库启动, param:{}", param);
        if (!hkTradingCalendarApi.isTradingDay(LocalDate.now())) {
            //非交易日不处理
            return ReturnT.SUCCESS;
        }
        long t = System.currentTimeMillis();
        int pageSize = StrUtil.isBlank(param) ? 1 : Integer.parseInt(param);
        List<SouthwardCapitalStatistics> southwardCapitalStatistics = BeanUtil.copyToList(this.getSouthwardCapitalStatistics(pageSize), SouthwardCapitalStatistics.class);
        if (!southwardCapitalStatistics.isEmpty()) {
            log.info("开始落库， size:{}", southwardCapitalStatistics.size());
            int ret = southwardCapitalStatisticsMapper.saveOrUpdateBatch(southwardCapitalStatistics);
            log.info("ret: {}", ret);
        }
        log.info("将南向资金落库, 总耗时:{}ms", System.currentTimeMillis() - t);
        return ReturnT.SUCCESS;
    }



    @XxlJob(value = "saveSouthwardCapitalStockDetailToRedisJob", author = "秦禧", desc = "将个股南向资金列表详情数据保存到redis")
    public ReturnT<String> saveSouthwardCapitalStockDetailToRedisJob(String param) {
        MDC.put("TraceId", IdUtil.simpleUUID());
        log.info("将个股南向资金保存到redis启动, param:{}", param);
        LocalDate today = LocalDate.now();
        long t = System.currentTimeMillis();
        //近61个交易日
        int tradeDayCount = 62;
        LocalDate startDate = LocalDate.parse(hkTradingCalendarApi.queryBeforeTradingCalendars(today, tradeDayCount).toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<StockSouthwardCapitalStatistics> southwardCapitalStatistics = stockSouthwardCapitalStatisticsMapper.selectByStatisticDate(startDate, today);
        Map<String, List<StockSouthwardCapitalStatistics>> stockDataMap = southwardCapitalStatistics.stream().collect(Collectors.groupingBy(StockSouthwardCapitalStatistics::getCode));
        Map<String, ComStockSimpleDto> stockInfoMap = stockCache.queryStockInfoMap(new ArrayList<>(stockDataMap.keySet()));
        List<SouthwardCapitalStockDetailResp> detailRespList = new ArrayList<>(CollUtil.size(southwardCapitalStatistics));
        //查所有港股通的股票
        List<String> hkStockCodeThroughList = xnhks0101Mapper.getHkStockCodeThrough();
        Collection<String> codeColl = CollUtil.filterNew(stockDataMap.keySet(), stockInfoMap::containsKey);
        for (String stockCode : codeColl) {
            SouthwardCapitalStockDetailResp detailResp = new SouthwardCapitalStockDetailResp();
            ComStockSimpleDto simpleDto = stockInfoMap.get(stockCode);
            //股票快照
            detailResp.setCode(stockCode);
            detailResp.setStockId(simpleDto.getStockId());
            detailResp.setName(simpleDto.getName());
            //上一个交易日南向资金数据
            List<StockSouthwardCapitalStatistics> stockStatisticList = stockDataMap.get(stockCode);
            log.info("stockCode = {} stockStatisticList.size = {}", stockCode, stockStatisticList.size());
            StockSouthwardCapitalStatistics lastTradingDayStatistic = stockStatisticList.get(0);
            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now());
            //是否是上个交易日的数据，港股通资金流入使用，拉不到上个交易日数据则填充0
            Boolean isLastTradingData = lastTradingDayStatistic.getStatisticsDate().equals(beforeTradingCalendar.getDate());
            detailResp.setTotalRank(CollUtil.size(hkStockCodeThroughList));
            detailResp.setNetTurnoverIn(isLastTradingData ?lastTradingDayStatistic.getTodayNetTurnoverIn():null);
            detailResp.setHoldingRate(lastTradingDayStatistic.getTodayHoldingRate());
            detailResp.setNetBuyingShares(isLastTradingData ? lastTradingDayStatistic.getTodayNetBuyingShares() :null);
            detailResp.setHoldingMarkValue(lastTradingDayStatistic.getTodayHoldingMarketValue());
            detailResp.setHoldingIncreaseRate(isLastTradingData ? NumberUtil.sub(lastTradingDayStatistic.getTodayHoldingRate(), stockStatisticList.get(Math.min(1, stockStatisticList.size() - 1)).getTodayHoldingRate()) : null);
            //近N日南向资金数据
            BigDecimal netTurnoverInNearly5Days = this.getSumValue(stockStatisticList, 5, StockSouthwardCapitalStatistics::getTodayNetTurnoverIn);
            BigDecimal netTurnoverInNearly20Days = this.getSumValue(stockStatisticList, 20, StockSouthwardCapitalStatistics::getTodayNetTurnoverIn);
            BigDecimal netTurnoverInNearly60Days = this.getSumValue(stockStatisticList, 60, StockSouthwardCapitalStatistics::getTodayNetTurnoverIn);
            BigDecimal netBuyingSharesNearly5Days = this.getSumValue(stockStatisticList, 5, StockSouthwardCapitalStatistics::getTodayNetBuyingShares);
            BigDecimal netBuyingSharesNearly20Days = this.getSumValue(stockStatisticList, 20, StockSouthwardCapitalStatistics::getTodayNetBuyingShares);
            BigDecimal netBuyingSharesNearly60Days = this.getSumValue(stockStatisticList, 60, StockSouthwardCapitalStatistics::getTodayNetBuyingShares);
            BigDecimal holdingIncreaseRateNearly5Days = NumberUtil.sub(lastTradingDayStatistic.getTodayHoldingRate(), stockStatisticList.get(Math.min(5, stockStatisticList.size() - 1)).getTodayHoldingRate());
            BigDecimal holdingIncreaseRateNearly20Days = NumberUtil.sub(lastTradingDayStatistic.getTodayHoldingRate(), stockStatisticList.get(Math.min(20, stockStatisticList.size() - 1)).getTodayHoldingRate());
            BigDecimal holdingIncreaseRateNearly60Days = NumberUtil.sub(lastTradingDayStatistic.getTodayHoldingRate(), stockStatisticList.get(Math.min(60, stockStatisticList.size() - 1)).getTodayHoldingRate());
            detailResp.setNetTurnoverInNearly5Days(netTurnoverInNearly5Days);
            detailResp.setNetTurnoverInNearly20Days(netTurnoverInNearly20Days);
            detailResp.setNetTurnoverInNearly60Days(netTurnoverInNearly60Days);
            detailResp.setNetBuyingSharesNearly5Days(netBuyingSharesNearly5Days);
            detailResp.setNetBuyingSharesNearly20Days(netBuyingSharesNearly20Days);
            detailResp.setNetBuyingSharesNearly60Days(netBuyingSharesNearly60Days);
            detailResp.setHoldingIncreaseRateNearly5Days(holdingIncreaseRateNearly5Days);
            detailResp.setHoldingIncreaseRateNearly20Days(holdingIncreaseRateNearly20Days);
            detailResp.setHoldingIncreaseRateNearly60Days(holdingIncreaseRateNearly60Days);
            detailResp.setStockType(simpleDto.getStockType());
            detailResp.setRegionType(simpleDto.getRegionType());
            detailRespList.add(detailResp);
            // stockDetailMap.put(stockCode, detailResp);
        }

        if (CollUtil.isNotEmpty(detailRespList)) {
            List<SouthwardCapitalStockDetailResp> lastTradingDatas = detailRespList.stream().filter(o -> null != o.getTotalRank()).collect(Collectors.toList());
            // 按资金流入排序
            // 昨日资金净流入排序
            sortDetailRespByTurnover(lastTradingDatas, SouthwardCapitalStockDetailResp::getNetTurnoverIn, SouthwardCapitalStockDetailResp::setTurnoverRank);
            // 5日资金净流入排序
            sortDetailRespByTurnover(detailRespList, SouthwardCapitalStockDetailResp::getNetTurnoverInNearly5Days, SouthwardCapitalStockDetailResp::setTurnoverRankNearly5Days);
            // 20日资金净流入排序
            sortDetailRespByTurnover(detailRespList, SouthwardCapitalStockDetailResp::getNetTurnoverInNearly20Days, SouthwardCapitalStockDetailResp::setTurnoverRankNearly20Days);
            // 600日资金净流入排序
            sortDetailRespByTurnover(detailRespList, SouthwardCapitalStockDetailResp::getNetTurnoverInNearly60Days, SouthwardCapitalStockDetailResp::setTurnoverRankNearly60Days);

            Map<String, SouthwardCapitalStockDetailResp> stockDetailMap = detailRespList.stream().collect(Collectors.toMap(SouthwardCapitalStockDetailResp::getCode, v -> v, (o, v) -> v));
            redisClient.del(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP);
            redisClient.hmset(RedisKeyConstants.BG_HK_SOUTHWARD_CAPITAL_STOCK_DETAIL_MAP, stockDetailMap, (long) 60 * 60 * 24 * 10);
            //将南向资金保存到redis结束, 总耗时:2261ms
            log.info("将个股南向资金保存到redis结束, 总耗时:{}ms", System.currentTimeMillis() - t);
        }
        return ReturnT.SUCCESS;
    }

    private void sortDetailRespByTurnover(List<SouthwardCapitalStockDetailResp> detailRespList, Function<SouthwardCapitalStockDetailResp, BigDecimal> function, BiConsumer<SouthwardCapitalStockDetailResp, Integer> biConsumer) {
        Map<BigDecimal, List<SouthwardCapitalStockDetailResp>> turnoverGroupMap = new LinkedHashMap<>();
        // 分组排序
        detailRespList.stream().filter(resp -> ObjectUtil.isNotEmpty(function.apply(resp))).collect(Collectors.groupingBy(function)).entrySet().stream()
                .sorted(Map.Entry.<BigDecimal, List<SouthwardCapitalStockDetailResp>>comparingByKey().reversed())
                .forEachOrdered(e -> turnoverGroupMap.put(e.getKey(), e.getValue()));
        int index = 1;
        for (BigDecimal turnover : turnoverGroupMap.keySet()) {
            List<SouthwardCapitalStockDetailResp> stockList = turnoverGroupMap.get(turnover);
            for (SouthwardCapitalStockDetailResp stockResp : stockList) {
                biConsumer.accept(stockResp, index);
            }
            index = index + CollUtil.size(stockList);
        }
    }

    private List<String> getHkStockThroughList(String stockCode) {
        List<String> stockCodeList = new ArrayList<>();
        //查所有港股通的股票
        List<String> hkStockCodeThroughList = xnhks0101Mapper.getHkStockCodeThrough();
        if (StrUtil.isBlank(stockCode)) {
            stockCodeList.addAll(hkStockCodeThroughList);
        } else {
            List<String> codeList = StrUtil.split(stockCode, ",");
            stockCodeList.addAll(CollUtil.intersection(hkStockCodeThroughList, codeList));
        }
        return stockCodeList;
    }

    private BigDecimal getSumValue(List<StockSouthwardCapitalStatistics> stockStatisticList, int limit, Function<StockSouthwardCapitalStatistics, BigDecimal> fun) {
        return stockStatisticList.stream()
                .limit(limit)
                .filter(item -> Objects.nonNull(fun.apply(item)))
                .map(fun)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SouthwardCapitalStatisticsDTO> getSouthwardCapitalStatistics(int pageSize) {
        String direction = "SB";
        List<String> marketList = new ArrayList<>();
        marketList.add(SouthwardCapitalMarketEnum.SH.getMarket());
        marketList.add(SouthwardCapitalMarketEnum.SZ.getMarket());
        List<SouthwardCapitalStatisticsDTO> southwardCapitalStatistics = new ArrayList<>(pageSize * marketList.size());
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        for (String market : marketList) {
            //key: date  value:entity
            Map<LocalDate, SouthwardCapitalStatisticsDTO> map = new HashMap<>(16);
            TurnoverFlowReqDTO turnoverFlowReq = TurnoverFlowReqDTO.builder()
                    .direction(direction)
                    .date(today)
                    .market(market)
                    .pageSize(pageSize)
                    .build();
            //净买入
            List<TurnoverFlowRespDTO> turnoverFlowList = hkStockThroughApi.turnoverFlowPageInfo(turnoverFlowReq).getData();
            log.info("turnoverFlowList:{}", JSONUtil.toJsonStr(turnoverFlowList));
            //净流入、余额
            ConnectBalanceReqDTO connectBalanceReq = ConnectBalanceReqDTO.builder()
                    .period(1)
                    .date(today)
                    .market(market)
                    .pageSize(pageSize)
                    .build();
            List<ConnectBalanceRespDTO> connectBalanceList = iHsStockBusinessApi.getConnectBalance(connectBalanceReq).getData();
            log.info("connectBalanceList:{}", JSONUtil.toJsonStr(connectBalanceList));
            for (TurnoverFlowRespDTO turnoverFlowResp : turnoverFlowList) {
                LocalDateTime time = LocalDateTime.parse(turnoverFlowResp.getTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDate date = time.toLocalDate();
                SouthwardCapitalStatisticsDTO statistics = map.getOrDefault(date, new SouthwardCapitalStatisticsDTO());
                statistics.setMarket(market);
                statistics.setNetBuyingTurnover(turnoverFlowResp.getNetBuyingTurnover());
                statistics.setTurnoverFlowRespTime(time);
                statistics.setStatisticsDate(time.toLocalDate());
                statistics.setCreateTime(LocalDateTime.now());
                statistics.setUpdateTime(LocalDateTime.now());
                map.put(date, statistics);
            }
            for (ConnectBalanceRespDTO connectBalanceResp : connectBalanceList) {
                LocalDateTime time = LocalDateTime.parse(connectBalanceResp.getTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDate date = time.toLocalDate();
                SouthwardCapitalStatisticsDTO statistics = map.getOrDefault(date, new SouthwardCapitalStatisticsDTO());
                statistics.setMarket(market);
                statistics.setNetTurnoverIn(connectBalanceResp.getNetTurnoverIn());
                statistics.setSurplusQuota(connectBalanceResp.getSurplusQuota());
                statistics.setConnectBalanceRespTime(time);
                statistics.setStatisticsDate(date);
                statistics.setCreateTime(LocalDateTime.now());
                statistics.setUpdateTime(LocalDateTime.now());
                map.put(date, statistics);
            }
            southwardCapitalStatistics.addAll(map.values());
        }
        southwardCapitalStatistics.removeIf(item -> !hkTradingCalendarApi.isTradingDay(item.getStatisticsDate()));
        southwardCapitalStatistics.forEach(item -> {
            item.setSurplusQuota(BigDecimalUtil.null2Zero(item.getSurplusQuota()));
            item.setNetTurnoverIn(BigDecimalUtil.null2Zero(item.getNetTurnoverIn()));
            item.setNetBuyingTurnover(BigDecimalUtil.null2Zero(item.getNetBuyingTurnover()));
        });
        return southwardCapitalStatistics;
    }

    @XxlJob(value = "checkCapitalInfoForTodayMinuteJob", author = "马玲", desc = "检查当日分时趋势图(0 5 16 ? * *)")
    public ReturnT<String> checkCapitalInfoForTodayMinuteJob(String param) {
        if (!hkTradingCalendarApi.isTradingDay(LocalDate.now())) {
            //非交易日不处理
            return ReturnT.SUCCESS;
        }
        LocalDate localDate;
        if(StringUtils.isEmpty(param)){
            localDate = LocalDate.now();
        }else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            localDate = LocalDate.parse(param, formatter);
        }
        List<CapitalSaveInfo> list = redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, localDate));
        int size = list.size();
        log.info("检查当日分时趋势图,size={}",size);
        if(southwardMinuteLimit != size){
            String dingMsg = String.format("【南向资金当日分时check异常】实际分时条数:%d 当日分时条数:%d", size,southwardMinuteLimit);
            DingUtil.alert(dingMsg);
        }
        return ReturnT.SUCCESS;
    }



    @XxlJob(value = "repairCapitalOneMinuteJob", author = "马玲", desc = "修复南向资金分时数据",cron = "0 0 9 * * ?")
    public ReturnT<String> repairCapitalOneMinuteJob(String param) {
        if (StringUtils.isEmpty(param)) {
            return ReturnT.SUCCESS;
        }
        String[] split = param.split(",");
        String dateString = split[0];
        String handleType = split[1];
        String handleField = split[2];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        long minuteLong = LocalDateTime.parse(dateString, formatter).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        String substrDate = dateString.substring(0, 10);
        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(substrDate, formatterDay);

        List<CapitalSaveInfo> list = redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, localDate));
        if (HANDLE_TYPE_ZERO.equals(handleType)) {
            List<Long> timeList = new ArrayList<>();
            timeList.add(minuteLong);
            list = handleData(timeList, list);
        } else if (HANDLE_TYPE_MINUTE.equals(handleType)) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(minuteLong), ZoneId.systemDefault());
            LocalDateTime previousMinute = localDateTime.minusMinutes(1);
            long previousTimestamp = previousMinute.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            CapitalSaveInfo capitalSaveInfoBefore = list.stream().filter(s -> s.getTime().compareTo(previousTimestamp) == 0).findFirst().orElse(null);
            CapitalSaveInfo capitalSaveInfo = list.stream().filter(s -> s.getTime().compareTo(minuteLong) == 0).findFirst().orElse(null);
            if (capitalSaveInfoBefore == null) {
                log.info("修复南向资金分时数据,上一分钟数据为空.previousTimestamp={},minuteLong={}", previousTimestamp, minuteLong);
                return ReturnT.SUCCESS;
            }
            if(capitalSaveInfo == null){
                log.info("修复南向资金分时数据,该分钟数据为空,正常不会出现该种场景.minuteLong={}", minuteLong);
                return ReturnT.SUCCESS;
            }
            if (HANDLE_FIELD_ALL.equals(handleField)) {
                log.info("修复南向资金分时数据,该分钟数据全量修复.minuteLong={}", minuteLong);
                capitalSaveInfo.setShNetBuyingTurnover(capitalSaveInfoBefore.getShNetBuyingTurnover());
                capitalSaveInfo.setSzNetBuyingTurnover(capitalSaveInfoBefore.getSzNetBuyingTurnover());
                capitalSaveInfo.setAllNetBuyingTurnover(capitalSaveInfoBefore.getAllNetBuyingTurnover());
                capitalSaveInfo.setShNetTurnoverIn(capitalSaveInfoBefore.getShNetTurnoverIn());
                capitalSaveInfo.setSzNetTurnoverIn(capitalSaveInfoBefore.getSzNetTurnoverIn());
                capitalSaveInfo.setAllNetTurnoverIn(capitalSaveInfoBefore.getAllNetTurnoverIn());
            } else if (HANDLE_FIELD_TURNOVER_FLOW.equals(handleField)) {
                log.info("修复南向资金分时数据,该分钟数据修复净买入字段.minuteLong={}", minuteLong);
                capitalSaveInfo.setShNetBuyingTurnover(capitalSaveInfoBefore.getShNetBuyingTurnover());
                capitalSaveInfo.setSzNetBuyingTurnover(capitalSaveInfoBefore.getSzNetBuyingTurnover());
                capitalSaveInfo.setAllNetBuyingTurnover(capitalSaveInfoBefore.getAllNetBuyingTurnover());
            } else if (HANDLE_FIELD_TURNOVER_IN.equals(handleField)) {
                log.info("修复南向资金分时数据,该分钟数据修复净流入字段.minuteLong={}", minuteLong);
                capitalSaveInfo.setShNetTurnoverIn(capitalSaveInfoBefore.getShNetTurnoverIn());
                capitalSaveInfo.setSzNetTurnoverIn(capitalSaveInfoBefore.getSzNetTurnoverIn());
                capitalSaveInfo.setAllNetTurnoverIn(capitalSaveInfoBefore.getAllNetTurnoverIn());
            } else {
                log.info("修复南向资金分时数据,处理字段输入错误.minuteLong={},handleField={}", minuteLong, handleField);
            }

        }
        list.sort(Comparator.comparing(CapitalSaveInfo::getTime));
        redisClient.set(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, localDate), list, (long) 60 * 60 * 24 * 10);
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "repairCapitalOneDayJob", author = "马玲", desc = "修复南向资金某日分时数据",cron = "0 0 9 * * ?")
    public ReturnT<String> repairCapitalOneDayJob(String param) {
        if(StringUtils.isEmpty(param)){
            return ReturnT.SUCCESS;
        }
        String[] split = param.split(",");
        String dateString = split[0];
        String handleTime = split[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        List<CapitalSaveInfo> list = redisClient.get(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, localDate));

        LocalDateTime morningStartTime = localDate.atTime(9, 30);
        LocalDateTime morningEndTime = localDate.atTime(12, 0);
        LocalDateTime afternoonStartTime = localDate.atTime(13, 1);
        LocalDateTime afternoonEndTime = localDate.atTime(16, 0);
        List<Long> timeList = new ArrayList<>();
        List<Long> morningTimeList = getTimeList(morningStartTime, morningEndTime);
        List<Long> afternoonTimeList = getTimeList(afternoonStartTime, afternoonEndTime);
        if(CERTAIN_DAY.equals(dateString)){
            afternoonEndTime = localDate.atTime(13, 59);
            afternoonTimeList = getTimeList(afternoonStartTime, afternoonEndTime);
        }
        timeList.addAll(morningTimeList);
        timeList.addAll(afternoonTimeList);

        if(HANDLE_AM.equals(handleTime)){
            list = handleData(morningTimeList, list);
        }
        if(HANDLE_PM.equals(handleTime)){
            list = handleData(afternoonTimeList, list);
        }
        if(HANDLE_ALL.equals(handleTime)){
            list = handleData(timeList, list);
        }
        list.sort(Comparator.comparing(CapitalSaveInfo::getTime));
        redisClient.set(String.format(RedisKeyConstants.SOUTHWARD_CAPITAL_TODAY, localDate), list, (long) 60 * 60 * 24 * 10);
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "repairCapitalMoreDayJob", author = "马玲", desc = "修复南向资金多日分日数据",cron = "0 0 9 * * ?")
    public ReturnT<String> repairCapitalMoreDayJob(String param) {
        if(StringUtils.isEmpty(param)){
            return ReturnT.SUCCESS;
        }
        String[] split = param.split(",");
        List<String> dateList = Arrays.asList(split);
        List<Long> timeList = new ArrayList<>();
        dateList.stream().forEach(d -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(d, formatter);
            long dateLong = localDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
            timeList.add(dateLong);
        });
        List<CapitalSaveInfo> capitalList = redisClient.get(RedisKeyConstants.SOUTHWARD_CAPITAL_SIXTY);
        handleData(timeList,capitalList);
        return ReturnT.SUCCESS;
    }

    private List<CapitalSaveInfo> handleData(List<Long> timeList,List<CapitalSaveInfo> list){
            list.stream().forEach(t -> {
                if(timeList.contains(t.getTime())){
                    t.setShNetBuyingTurnover(new BigDecimal(0));
                    t.setSzNetBuyingTurnover(new BigDecimal(0));
                    t.setAllNetBuyingTurnover(new BigDecimal(0));
                    t.setShNetTurnoverIn(new BigDecimal(0));
                    t.setSzNetTurnoverIn(new BigDecimal(0));
                    t.setAllNetTurnoverIn(new BigDecimal(0));
                }
            });
        return list;
    }


    private List<Long> getTimeList(LocalDateTime start,LocalDateTime end){
        List<Long> timestampList = new ArrayList<>();
        LocalDateTime currentDateTime = start;
        while (currentDateTime.isBefore(end) || currentDateTime.equals(end)) {
            long timestamp = currentDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            timestampList.add(timestamp);
            currentDateTime = currentDateTime.plusMinutes(1);
        }
        return timestampList;
    }

}
