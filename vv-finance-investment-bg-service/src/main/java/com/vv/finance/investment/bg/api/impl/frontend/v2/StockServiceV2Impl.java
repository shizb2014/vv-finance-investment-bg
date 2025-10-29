package com.vv.finance.investment.bg.api.impl.frontend.v2;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenlibao.security.sdk.ws.core.model.req.IndexReq;
import com.fenlibao.security.sdk.ws.core.model.req.MinkTargetReq;
import com.fenlibao.security.sdk.ws.core.model.req.MinuteKReq;
import com.fenlibao.security.sdk.ws.core.model.req.TargetReq;
import com.google.common.collect.Lists;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.ConnectTurnoverConstants;
import com.vv.finance.common.entity.common.CommonTradeCapital;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.common.ComDDENetVo;
import com.vv.finance.common.entity.receiver.ConnectTurnover;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.v2.StockServiceV2;
import com.vv.finance.investment.bg.api.quotation.IQuotationService;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.api.stock.cache.StockKlineCacheApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.CapitalDistributionDaysEnum;
import com.vv.finance.investment.bg.dto.DdePolicyDto;
import com.vv.finance.investment.bg.dto.MoneyFlowDto;
import com.vv.finance.investment.bg.dto.info.*;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;
import com.vv.finance.investment.bg.entity.trade.TradeStatisticsDetail;
import com.vv.finance.investment.bg.stock.trade.mapper.StockTradeStatisticsDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenyu
 * @date 2021/3/17 14:27
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@RequiredArgsConstructor
public class StockServiceV2Impl implements StockServiceV2 {

    @Resource
    private IStockMarketService stockMarketService;

    @Autowired
    private HkTradingCalendarApi tradingCalendarApi;

    @Resource
    StockTradeStatisticsDetailMapper stockTradeStatisticsDetailMapper;



    private final StockKlineCacheApi stockKlineCacheApi;

    private final IQuotationService quotationService;

    @Autowired
    private StockTradeStatisticsApi tradeStatisticsApi;

    @Autowired
    IStockKlineService iStockKlineService;
    @Autowired
    private StockInfoApi stockInfoApi;



    @Value("${kline.source}")
    private Integer source;
    @Value("#{'${hk.index.code}'.split(',')}")
    private List<String> indexs;
    //这个配置的时候一定要配置功能上线的那天时间
    @Value("${trade.deal.suf.date:20230712}")
    private String tradeDealSufDate;

    private static final int FIVE_DAY = 5;
    private static final LocalTime NINE = LocalTime.of(9, 0);

    @Autowired
    private RedisClient redisClient;

//    @Override
    /*public CapitalDistributionVo getCapitalDistributionOld(String stockCode) {
        // 查询所有资金分布
        CapitalDistributionVo resultVo = new CapitalDistributionVo();
        List<Object> objects = redisClient.lGet(RedisKeyConstants.TRADE_LIST_CAPITAL_DISTRIBUTION + stockCode, 0, -1);
        if (CollectionUtil.isEmpty(objects)) {
            log.info("股票代码:" + stockCode + " 查询不到资金分布，请检查！！！！！");
            return resultVo;
        }
        Long time = ((TradeStatisticsDto) objects.get(0)).getTime();
        // 交易日的盘前竞价
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        LocalDate date = LocalDate.now();
        if (!tradingDay) {
            date = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
        }
        long nineClock = DateUtils.getNineClock(date);
        boolean bidding = DateUtils.bidding();
        boolean tradingDayBidding = bidding && tradingDay;
        // 九点后是否有数据
        boolean hasDate = time > nineClock;

        List<CapitalDistributionDTO> capitalDistributionDTOS = new ArrayList<>();
        int size = objects.size();
        CapitalDistributionDTO dto;
        for (CapitalDistributionDaysEnum value : CapitalDistributionDaysEnum.values()) {
            if (!tradingDayBidding) {
                // 非交易日 或非盘前竞价
                dto = objects.subList(0, Math.min(size, value.getDays())).stream().map(c ->
                        buildCapitalDistributionDTO((TradeStatisticsDto) c)).reduce(CapitalDistributionDTO::add).get();
            } else if (value == CapitalDistributionDaysEnum.ONE_DAY) {
                // 盘前竞价不显示今天的数据
                dto = new CapitalDistributionDTO();
            } else if (size == 1 && hasDate) {
                // 有且只有一条盘前竞价的数据
                dto = new CapitalDistributionDTO();
            } else if (size == 1 && !hasDate) {
                // 仅有前一天的数据
                dto = buildCapitalDistributionDTO((TradeStatisticsDto) objects.get(0));
            } else if (hasDate) {
                // 盘前竞价有数据 需要过滤掉盘前竞价的数据
                dto = objects.subList(1, Math.min(size, value.getDays())).stream().map(c ->
                        buildCapitalDistributionDTO((TradeStatisticsDto) c)).reduce(CapitalDistributionDTO::add).get();
            } else {
                // 交易日盘前竞价没有数据
                dto = objects.subList(0, Math.min(size, value.getDays()) - 1).stream().map(c ->
                        buildCapitalDistributionDTO((TradeStatisticsDto) c)).reduce(CapitalDistributionDTO::add).get();
            }
            dto.setCode(stockCode);
            dto.setType(value.getCode());
            capitalDistributionDTOS.add(dto);
        }
        resultVo.setCapitalDistributionList(capitalDistributionDTOS);
        // 盘前竞价的更新时间为9点
        // 交易日没有数据的话，更新时间为九点
        // 交易日盘前竞价的话，更新时间也为九点
        resultVo.setTime(tradingDay && (bidding || hasDate) ? nineClock : time);
        return resultVo;
//        return getCapitalDistribution2(stockCode);
    }*/

    @Override
    public CapitalDistributionVo getCapitalDistribution(String stockCode) {
        CapitalDistributionVo resultVo = new CapitalDistributionVo();
        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listSixtyRealTime(stockCode, tradingDay, Boolean.FALSE);
        Long updateTime = tradeStatistics.size() == 0 ? 0 : tradeStatistics.get(0).getTime();
        List<CapitalDistributionDTO> capitalDistributionDTOS = new ArrayList<>();
        int size = tradeStatistics.size();
        CapitalDistributionDTO dto;
        for (CapitalDistributionDaysEnum value : CapitalDistributionDaysEnum.values()) {
            if (value == CapitalDistributionDaysEnum.ONE_DAY) {
                dto = buildCapitalDistributionDTO(tradeStatistics.get(0));
            } else {
                dto = tradeStatistics.subList(0, Math.min(size, value.getDays())).stream().map(c ->
                        buildCapitalDistributionDTO(c)).reduce(CapitalDistributionDTO::add).get();
            }
            dto.setCode(stockCode);
            dto.setType(value.getCode());
            capitalDistributionDTOS.add(dto);
        }
        resultVo.setCapitalDistributionList(capitalDistributionDTOS);
        resultVo.setTime(updateTime);
        return resultVo;
    }

    private CapitalDistributionDTO buildCapitalDistributionDTO(TradeStatistics tradeStatistics) {
        CapitalDistributionDTO capitalDistributionDTO = new CapitalDistributionDTO();
        capitalDistributionDTO.setCapitalInXLarge(tradeStatistics.getInCapitalXLarge());
        capitalDistributionDTO.setCapitalInLarge(tradeStatistics.getInCapitalLarge());
        capitalDistributionDTO.setCapitalInMid(tradeStatistics.getInCapitalMid());
        capitalDistributionDTO.setCapitalInSmall(tradeStatistics.getInCapitalSmall());
        capitalDistributionDTO.setCapitalInTotal(tradeStatistics.getInCapitalTotal());

        capitalDistributionDTO.setCapitalOutXLarge(tradeStatistics.getOutCapitalXLarge());
        capitalDistributionDTO.setCapitalOutLarge(tradeStatistics.getOutCapitalLarge());
        capitalDistributionDTO.setCapitalOutMid(tradeStatistics.getOutCapitalMid());
        capitalDistributionDTO.setCapitalOutSmall(tradeStatistics.getOutCapitalSmall());
        capitalDistributionDTO.setCapitalOutTotal(tradeStatistics.getOutCapitalTotal());
        return capitalDistributionDTO;
    }


    @Override
    public ConnectTurnoverDTO getConnectTurnover(String direction) {
        List<ConnectTurnover> metadata = Objects.requireNonNull(redisClient.lGet(RedisKeyConstants.RECEIVER_CONNECT_TURNOVER_LIST, 0, -1)).stream().map(item -> (ConnectTurnover) item).collect(Collectors.toList());
        List<ConnectTurnover> connectTurnovers = metadata.stream().filter(item -> item.getDirection().equals(direction)).collect(Collectors.toList());
        String pattern = "yyyyMMddHHmmssSSS";
        List<ConnectTurnoverBase> sz = connectTurnovers.stream().filter(item -> item.getMarket().equals(ConnectTurnoverConstants.SZ)).map(item -> {
            ConnectTurnoverBase connectTurnoverBase = new ConnectTurnoverBase();
            connectTurnoverBase.setDirection(item.getDirection());
            connectTurnoverBase.setMarket(item.getMarket());
            try {
                connectTurnoverBase.setTime(DateUtils.parseDate(item.getTime(), pattern).getTime());
            } catch (ParseException e) {
                log.error("日期转换失败！！", e);
            }
            BigDecimal buy = new BigDecimal(item.getBuyturnover());
            BigDecimal sell = new BigDecimal(item.getSellturnover());
            connectTurnoverBase.setNetTurnover(buy.subtract(sell));
            return connectTurnoverBase;
        }).collect(Collectors.toList());
        sz.sort(Comparator.comparing(ConnectTurnoverBase::getTime));
        List<ConnectTurnoverBase> sh = connectTurnovers.stream().filter(item -> item.getMarket().equals(ConnectTurnoverConstants.SH)).map(item -> {
            ConnectTurnoverBase connectTurnoverBase = new ConnectTurnoverBase();
            connectTurnoverBase.setDirection(item.getDirection());
            connectTurnoverBase.setMarket(item.getMarket());
            try {
                connectTurnoverBase.setTime(DateUtils.parseDate(item.getTime(), pattern).getTime());
            } catch (ParseException e) {
                log.error("日期转换失败！！", e);
            }
            BigDecimal buy = new BigDecimal(item.getBuyturnover());
            BigDecimal sell = new BigDecimal(item.getSellturnover());
            connectTurnoverBase.setNetTurnover(buy.subtract(sell));
            return connectTurnoverBase;
        }).collect(Collectors.toList());
        sz.sort(Comparator.comparing(ConnectTurnoverBase::getTime));
        sh.sort(Comparator.comparing(ConnectTurnoverBase::getTime));
        ConnectTurnoverDTO connectTurnoverDTO = new ConnectTurnoverDTO();
        connectTurnoverDTO.setShTurnOver(sh);
        connectTurnoverDTO.setSzTurnover(sz);
        return connectTurnoverDTO;
    }


    private BgTradingCalendar getBgTradingCalendar(List<BgTradingCalendar> tradingCalendars, int i) {
        return tradingCalendars.get(i);
    }

//    private List<BaseKlineDTO> buildTrendKlineMapByRJH(List<KlineResp> kline, List<StockTargetResp> target, @NotNull String code) {
//        if (kline == null || kline.size() < 1) {
//            return Collections.emptyList();
//        }
//        // 计算昨收价
//        KlineResp klineResp = kline.get(0);
//        BigDecimal preClose = klineResp.getPreClose();
//
//        List<KlineEntity> klineDTOList;
//        if (code.contains(".hk")) {
//            AtomicReference<BigDecimal> totalVol = new AtomicReference<>(new BigDecimal(0));
//            AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(new BigDecimal(0));
//            klineDTOList = kline.stream().map(item -> {
//                totalAmount.getAndSet(totalAmount.get().add(item.getAmount()));
//                totalVol.getAndSet(totalVol.get().add(item.getVolume()));
//
//                KlineEntity baseKlineDTO = new KlineEntity();
//                baseKlineDTO.setTimeStr(DateUtils.formatDate(item.getTime(), "yyyy-MM-dd"));
//                baseKlineDTO.setOpen(item.getOpen());
//                baseKlineDTO.setClose(item.getClose());
//                baseKlineDTO.setHigh(item.getHigh());
//                baseKlineDTO.setLow(item.getLow());
//                if (BigDecimal.ZERO.compareTo(totalVol.get()) == 0) {
//                    baseKlineDTO.setVwap(item.getClose());
//                } else {
//                    baseKlineDTO.setVwap(totalAmount.get().divide(totalVol.get(), 3, RoundingMode.HALF_UP));
//                }
//                baseKlineDTO.setPreClose(preClose);
//                baseKlineDTO.setChg(item.getChg());
//                baseKlineDTO.setChgPct(item.getChg_pct());
//                baseKlineDTO.setVolume(item.getVolume());
//                baseKlineDTO.setAmount(item.getAmount());
//                baseKlineDTO.setTime(item.getTime().getTime());
//                return baseKlineDTO;
//            }).collect(Collectors.toList());
//        } else {
//            AtomicReference<BigDecimal> totalClose = new AtomicReference<>(new BigDecimal(0));
//            AtomicReference<BigDecimal> index = new AtomicReference<>(new BigDecimal(0));
//            klineDTOList = kline.stream().map(item -> {
//                totalClose.getAndSet(totalClose.get().add(item.getClose()));
//                index.getAndSet(index.get().add(BigDecimal.ONE));
//                KlineEntity baseKlineDTO = new KlineEntity();
//                baseKlineDTO.setTimeStr(DateUtils.formatDate(item.getTime(), "yyyy-MM-dd"));
//                baseKlineDTO.setOpen(item.getOpen());
//                baseKlineDTO.setClose(item.getClose());
//                baseKlineDTO.setHigh(item.getHigh());
//                baseKlineDTO.setLow(item.getLow());
//                baseKlineDTO.setVwap(totalClose.get().divide(index.get(), 3, RoundingMode.HALF_UP));
//                baseKlineDTO.setPreClose(preClose);
//                baseKlineDTO.setChg(item.getChg());
//                baseKlineDTO.setChgPct(item.getChg_pct());
//                baseKlineDTO.setVolume(item.getVolume());
//                baseKlineDTO.setAmount(item.getAmount());
//                baseKlineDTO.setTime(item.getTime().getTime());
//                return baseKlineDTO;
//            }).collect(Collectors.toList());
//        }
//        List<TechnicalIndicatorsEntity> indicatorsEntities = target.stream().map(item -> {
//            TechnicalIndicatorsEntity technicalIndicatorsEntity = new TechnicalIndicatorsEntity();
//            technicalIndicatorsEntity.setCode(item.getCode());
//            technicalIndicatorsEntity.setTime(item.getDate());
//            technicalIndicatorsEntity.setTimeStr(item.getTime());
//            SAREntity sarEntity = new SAREntity();
//            sarEntity.setSarUp(item.getSarUp() == null ? 0 : item.getSarUp().intValue());
//            sarEntity.setSar(item.getSar());
//            sarEntity.setSarFacto(item.getSarFacto());
//            technicalIndicatorsEntity.setSar(sarEntity);
//            OBVEntity obvEntity = new OBVEntity();
//            obvEntity.setObv(item.getObv() == null ? 0 : item.getObv().intValue());
//            technicalIndicatorsEntity.setObv(obvEntity);
//            return technicalIndicatorsEntity;
//        }).collect(Collectors.toList());
//        List<BaseKlineDTO> klineDTOList1 = mergeKlineEntity(null, klineDTOList, indicatorsEntities, null, true);
//        return klineDTOList1;
//    }


    @Override
    public List<DDENetVo> listDDENet(String stockCode) {
        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listSixtyRealTime(stockCode, tradingDay, Boolean.FALSE);

        if (CollectionUtil.isEmpty(tradeStatistics)) {
            log.info("股票代码:{} 查询不到资金分布，请检查！！！！！", stockCode);
            return Collections.emptyList();
        }
        return tradeStatistics.stream().map(ts -> {
            DDENetVo ddeNetVo = new DDENetVo();
            ddeNetVo.setDate(ts.getTime());
            // 主力资金包含特大单, 大单
            BigDecimal add = ts.getInCapitalXLarge().add(ts.getInCapitalLarge()).subtract(ts.getOutCapitalLarge()).subtract(ts.getOutCapitalXLarge());
            //不足60日数据应该返回null,而不是0，0在行情中是有意义的值
//            if (ts.getInCapitalXLarge().compareTo(BigDecimal.ZERO) == 0 && ts.getInCapitalLarge().compareTo(BigDecimal.ZERO) == 0
//                    && ts.getOutCapitalLarge().compareTo(BigDecimal.ZERO) == 0 && ts.getOutCapitalXLarge().compareTo(BigDecimal.ZERO) == 0) {
//                ddeNetVo.setNetInflow(null);
//            } else {
//                ddeNetVo.setNetInflow(add);
//            }
            ddeNetVo.setNetInflow(add);
            return ddeNetVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ComDDENetVo> listDDENetByTime(String stockCode, Long startTime, Long endTime) {

        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listStaticTradeStatistics(stockCode, tradingDay, startTime, endTime);

        if (CollectionUtil.isEmpty(tradeStatistics)) {
            log.info("入参股票：{}，开始时间{}，结束时间：{}",  stockCode, startTime, endTime);
            return Collections.emptyList();
        }
        return tradeStatistics.stream().filter(ts -> ts.getTime() >= startTime && ts.getTime() <= endTime).map(ts -> {
            ComDDENetVo ddeNetVo = new ComDDENetVo();
            ddeNetVo.setDate(ts.getTime());
            // 主力资金包含特大单, 大单
            BigDecimal add = ts.getInCapitalXLarge().add(ts.getInCapitalLarge()).subtract(ts.getOutCapitalLarge()).subtract(ts.getOutCapitalXLarge());
            ddeNetVo.setNetInflow(add);
            return ddeNetVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NetRankVo> getTotalNetRank(String code) {
        // 非交易日或者未开盘取前一日数据
        // 交易日开盘后取最近数据
        // 盘前竞价不展示今日数据
        LocalDate date = LocalDate.now();
        boolean tradingDay = tradingCalendarApi.isTradingDay(date);
        if (DateUtils.beforeNineHour() || !tradingDay) {
            date = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
        }
        NetRankVo todayVo = NetRankVo.builder().days(CapitalDistributionDaysEnum.ONE_DAY.getDays()).build();
        NetRankVo fiveVo = NetRankVo.builder().days(CapitalDistributionDaysEnum.FIVE_DAYS.getDays()).build();
        NetRankVo tenVo = NetRankVo.builder().days(CapitalDistributionDaysEnum.TEN_DAYS.getDays()).build();
        NetRankVo twentyVo = NetRankVo.builder().days(CapitalDistributionDaysEnum.TWENTY_DAYS.getDays()).build();
        NetRankVo sixtyVo = NetRankVo.builder().days(CapitalDistributionDaysEnum.SIXTY_DAYS.getDays()).build();

        // 展示今日的数据 1.交易日的非盘前竞价;2.非交易日
        boolean addToday = !DateUtils.bidding() || !tradingDay;
        Map<String, TodayCapitalTotalDto> todayMap = null;
        if (addToday) {
            todayMap = tradeStatisticsApi.getAllTodayTotalAmountFromRedis(date, Boolean.FALSE);
            if (MapUtil.isEmpty(todayMap)) {
                todayMap = new HashMap<>();
            }
            TodayCapitalTotalDto todayCapitalTotalDto = todayMap.getOrDefault(code, null);
            BigDecimal todayNet = todayCapitalTotalDto == null ? BigDecimal.ZERO : todayCapitalTotalDto.getNet();
            //将金额组成list去重
            Set<BigDecimal> todayNetList = new HashSet<>();
            for (TodayCapitalTotalDto todayCapitalTotalDto1 : todayMap.values()) {
                todayNetList.add(todayCapitalTotalDto1.getNet().stripTrailingZeros());
            }
            long todayRank = todayNetList.stream().filter(t -> t.compareTo(todayNet) > 0).count() + 1;
            todayVo.setRank((int) todayRank);
            todayVo.setNetInflow(todayNet);
        } else {
            todayMap = new HashMap<>(0);
        }

        // 累计流入
        Map<String, TotalCapitalInflowsDTO> totalMap = tradeStatisticsApi.getTotalCapitalInflowsFromRedis(date, Boolean.FALSE);
        if (MapUtil.isEmpty(totalMap)) {
            return Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo);
        }
        TotalCapitalInflowsDTO totalCapitalInflowsDTO = totalMap.getOrDefault(code, null);
        if (totalCapitalInflowsDTO == null) {
            totalCapitalInflowsDTO = new TotalCapitalInflowsDTO();
            totalCapitalInflowsDTO.setStockCode(code);
            totalCapitalInflowsDTO.setTenDaysTotal(BigDecimal.ZERO);
            totalCapitalInflowsDTO.setFiveDaysTotal(BigDecimal.ZERO);
            totalCapitalInflowsDTO.setSixtyDaysTotal(BigDecimal.ZERO);
            totalCapitalInflowsDTO.setTwentyDaysTotal(BigDecimal.ZERO);
            totalMap.put(code, totalCapitalInflowsDTO);
            //初始化为0
//            return Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo);
        }
//        if (addToday) {
        for (TotalCapitalInflowsDTO total : totalMap.values()) {
            TodayCapitalTotalDto todayCapitalTotal = todayMap.getOrDefault(total.getStockCode(), null);
            if (ObjectUtils.isEmpty(todayCapitalTotal)) {
                continue;
            }
            total.setFiveDaysTotal(total.getFiveDaysTotal().add(todayCapitalTotal.getNet()));
            total.setTenDaysTotal(total.getTenDaysTotal().add(todayCapitalTotal.getNet()));
            total.setTwentyDaysTotal(total.getTwentyDaysTotal().add(todayCapitalTotal.getNet()));
            total.setSixtyDaysTotal(total.getSixtyDaysTotal().add(todayCapitalTotal.getNet()));
        }

        TotalCapitalInflowsDTO finalTotalCapitalInflowsDTO = totalCapitalInflowsDTO;
        //将金额组成list去重
        Set<BigDecimal> fiveDaysList = new HashSet<>();
        for (TotalCapitalInflowsDTO totalCapitalInflowsUsDTO : totalMap.values()) {
            fiveDaysList.add(totalCapitalInflowsUsDTO.getFiveDaysTotal().stripTrailingZeros());
        }
        long fiveDaysRank = fiveDaysList.stream().filter(t -> t.compareTo(finalTotalCapitalInflowsDTO.getFiveDaysTotal()) > 0).count() + 1;
        fiveVo.setRank((int) fiveDaysRank);
        fiveVo.setNetInflow(totalCapitalInflowsDTO.getFiveDaysTotal());

        Set<BigDecimal> tenDaysList = new HashSet<>();
        for (TotalCapitalInflowsDTO totalCapitalInflowsUsDTO : totalMap.values()) {
            tenDaysList.add(totalCapitalInflowsUsDTO.getTenDaysTotal().stripTrailingZeros());
        }
        long tenDaysRank = tenDaysList.stream().filter(t -> t.compareTo(finalTotalCapitalInflowsDTO.getTenDaysTotal()) > 0).count() + 1;
        tenVo.setRank((int) tenDaysRank);
        tenVo.setNetInflow(totalCapitalInflowsDTO.getTenDaysTotal());

        Set<BigDecimal> twentyDaysList = new HashSet<>();
        for (TotalCapitalInflowsDTO totalCapitalInflowsUsDTO : totalMap.values()) {
            twentyDaysList.add(totalCapitalInflowsUsDTO.getTwentyDaysTotal().stripTrailingZeros());
        }
        long twentyDaysRank = twentyDaysList.stream().filter(t -> t.compareTo(finalTotalCapitalInflowsDTO.getTwentyDaysTotal()) > 0).count() + 1;
        twentyVo.setRank((int) twentyDaysRank);
        twentyVo.setNetInflow(totalCapitalInflowsDTO.getTwentyDaysTotal());

        Set<BigDecimal> sixtyDaysList = new HashSet<>();
        for (TotalCapitalInflowsDTO totalCapitalInflowsUsDTO : totalMap.values()) {
            sixtyDaysList.add(totalCapitalInflowsUsDTO.getSixtyDaysTotal().stripTrailingZeros());
        }
        long sixtyDaysRank = sixtyDaysList.stream().filter(t -> t.compareTo(finalTotalCapitalInflowsDTO.getSixtyDaysTotal()) > 0).count() + 1;
        sixtyVo.setRank((int) sixtyDaysRank);
        sixtyVo.setNetInflow(totalCapitalInflowsDTO.getSixtyDaysTotal());
        return Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo);
    }


    @Override
    public SimplePageResp<DdePolicyDto> listDDePolicy(SimplePageReq simplePageReq) {
        List<StockSnapshot> allStockSnapshot = quotationService.getAllStockSnapshot();
        SimplePageResp simplePageResp = new SimplePageResp();
        simplePageResp.setTotal((long) allStockSnapshot.size());
        simplePageReq.setPageSize(simplePageReq.getPageSize());
        simplePageReq.setCurrentPage(simplePageReq.getCurrentPage());
        List<DdePolicyDto> collect = allStockSnapshot.stream().map(item -> {
            DdePolicyDto ddePolicyDto = new DdePolicyDto();
            BeanUtils.copyProperties(item, ddePolicyDto);
            return ddePolicyDto;
        }).collect(Collectors.toList());
        simplePageResp.setRecord(collect);

        return simplePageResp;
    }

    @Override
    public SimplePageResp<MoneyFlowDto> listMoneyFlow(SimplePageReq simplePageReq) {
        List<StockSnapshot> allStockSnapshot = quotationService.getAllStockSnapshot();
        SimplePageResp simplePageResp = new SimplePageResp();
        simplePageResp.setTotal((long) allStockSnapshot.size());
        simplePageReq.setPageSize(simplePageReq.getPageSize());
        simplePageReq.setCurrentPage(simplePageReq.getCurrentPage());
        List<MoneyFlowDto> collect = allStockSnapshot.stream().map(item -> {
            MoneyFlowDto moneyFlowDto = new MoneyFlowDto();
            BeanUtils.copyProperties(item, moneyFlowDto);
            return moneyFlowDto;
        }).collect(Collectors.toList());
        simplePageResp.setRecord(collect);

        return simplePageResp;
    }


    @Override
    public Order getStockOrder(String stockCode) {
        Order order = (Order) redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(stockCode));
        if (ObjectUtils.isNotEmpty(order) && StringUtils.isNotEmpty(order.getTime())) {
            Long time = Long.valueOf(order.getTime().substring(0, 12));
            LocalDate date = LocalDate.now();
            boolean tradingDay = tradingCalendarApi.isTradingDay(date);
            LocalDateTime bidding;
            if (!tradingDay || DateUtils.beforeNineHour()) {
                LocalDate beforeTrading = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
                bidding = LocalDateTime.of(beforeTrading, LocalTime.of(9, 00));
            } else {
                bidding = LocalDateTime.of(date, LocalTime.of(9, 00));
            }
            Long biddingTime = DateUtils.formatLocalDateTimeToLong(bidding);
            if (time < biddingTime) {
                return null;
            } else {
                return order;
            }
        } else {
            return order;
        }
    }


    /**
     * 批量获取盘口数据
     * number = -1 时，获取所有盘口数据
     *
     * @param codes 股票代码集合
     * @return List<Order>
     */
    @Override
    public List<Order> getStockOrderByCodes(List<String> codes, Integer number) {
        log.info("从redis中批量获取港股订单快照信息，请求codes：【{}】", codes);
        List<String> keys = codes.stream().map(o -> RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(o)).collect(Collectors.toList());
        List<Order> orders = redisClient.batchGet(keys);
        if (number != -1) {
            if (CollUtil.isNotEmpty(orders)) {
                LocalDate date = LocalDate.now();
                boolean tradingDay = tradingCalendarApi.isTradingDay(date);
                LocalDateTime bidding;
                if (!tradingDay || DateUtils.beforeNineHour()) {
                    LocalDate beforeTrading = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
                    bidding = LocalDateTime.of(beforeTrading, LocalTime.of(9, 00));
                } else {
                    bidding = LocalDateTime.of(date, LocalTime.of(9, 00));
                }
                Long biddingTime = DateUtils.formatLocalDateTimeToLong(bidding);
                Iterator<Order> iterator = orders.iterator();
                while (iterator.hasNext()) {
                    Order order = iterator.next();
                    if (StringUtils.isNotEmpty(order.getTime())) {
                        Long time = Long.valueOf(order.getTime().substring(0, 12));
                        if (time < biddingTime) {
                            iterator.remove();
                        } else {
                            order.setAsklist(CollectionUtils.isNotEmpty(order.getAsklist()) ? order.getAsklist().stream().limit(number).collect(Collectors.toList()) : Collections.EMPTY_LIST);
                            order.setBidlist(CollectionUtils.isNotEmpty(order.getBidlist()) ? order.getBidlist().stream().limit(number).collect(Collectors.toList()) : Collections.EMPTY_LIST);
                        }
                    }
                }
            }
        }
        return orders;
    }

    @Override
    public List<CommonTradeCapital> listDDEForQuant(String stockCode, LocalDate date, Integer todayFlag) {
        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listTradeStatisticsRealTime(stockCode, tradingDay, Boolean.FALSE, date, todayFlag);

        if (CollectionUtil.isEmpty(tradeStatistics)) {
            log.info("股票代码:{} 查询不到资金分布，请检查！！！！！", stockCode);
            return Collections.emptyList();
        }
        return tradeStatistics.stream().map(ts -> {
            CommonTradeCapital ddeNetVo = new CommonTradeCapital();
            ddeNetVo.setTime(ts.getTime());
            ddeNetVo.setCapitalIn(ts.getInCapitalXLarge().add(ts.getInCapitalLarge()));
            ddeNetVo.setCapitalOut(ts.getOutCapitalLarge().add(ts.getOutCapitalXLarge()));
            // 主力资金包含特大单, 大单
            BigDecimal add = ts.getInCapitalXLarge().add(ts.getInCapitalLarge()).subtract(ts.getOutCapitalLarge()).subtract(ts.getOutCapitalXLarge());
            //不足60日数据应该返回null,而不是0，0在行情中是有意义的值
//            if (ts.getInCapitalXLarge().compareTo(BigDecimal.ZERO) == 0 && ts.getInCapitalLarge().compareTo(BigDecimal.ZERO) == 0
//                    && ts.getOutCapitalLarge().compareTo(BigDecimal.ZERO) == 0 && ts.getOutCapitalXLarge().compareTo(BigDecimal.ZERO) == 0) {
//                ddeNetVo.setNetInflow(null);
//            } else {
//                ddeNetVo.setNetInflow(add);
//            }
            ddeNetVo.setCapitalNet(add);
            return ddeNetVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TradeStatisticsDetail> listDDEByTime(String stockCode,long startTime, long endTime) {
        List<TradeStatisticsDetail> commonTradeCapitals =  stockTradeStatisticsDetailMapper.selectList(new QueryWrapper<TradeStatisticsDetail>()
                .eq("code", stockCode)
                .lt("time", endTime)
                .ge("time", startTime)
        );
        return commonTradeCapitals;
    }

    @Override
    public Boolean saveOrUpdateBatchTradeStatic(List<TradeStatisticsDetail> tradeStatisticsDetails) {
        stockTradeStatisticsDetailMapper.batchSaveOrUpdate(tradeStatisticsDetails);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delDdeBySizeCriterion(String stockCode, long time) {
        stockTradeStatisticsDetailMapper.delete(new QueryWrapper<TradeStatisticsDetail>()
                .eq("code", stockCode)
                .lt("time", time)
                .ge("time", time - 86400000)
        );
        return Boolean.TRUE;
    }

}
