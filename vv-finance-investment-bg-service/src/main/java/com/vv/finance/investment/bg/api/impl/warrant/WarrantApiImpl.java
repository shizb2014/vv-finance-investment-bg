package com.vv.finance.investment.bg.api.impl.warrant;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.TradeDirectionConstants;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.api.warrant.WarrantApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.CapitalDistributionDaysEnum;
import com.vv.finance.investment.bg.dto.info.*;
import com.vv.finance.investment.bg.dto.warrant.WarrantCodeVo;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.service.IStockRelatedDetailsService;
import com.vv.finance.investment.bg.warant.service.WarrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName WarrantApiImpl
 * @Deacription 权证API实现类
 * @Author lh.sz
 * @Date 2021年12月21日 13:49
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class WarrantApiImpl implements WarrantApi {
    @Resource
    RedisClient redisClient;
    @Resource
    WarrantService warrantService;
    @Resource
    HkTradingCalendarApi tradingCalendarApi;
    @Resource
    IStockRelatedDetailsService stockRelatedDetailsService;
    @Autowired
    private StockTradeStatisticsApi tradeStatisticsApi;

    private static final String BUY = "B";

    @Override
    public ResultT<OrderBrokerDto> getOrderBroker(String warrantCode,
                                                  String type) {
        OrderBrokerDto orderBrokerDto = new OrderBrokerDto();
        String json = BUY.equals(type) ? redisClient.hget(RedisKeyConstants.RECEIVER_WARRANT_MAP_ORDER_BROKER_BUY, warrantCode)
                : redisClient.hget(RedisKeyConstants.RECEIVER_WARRANT_MAP_ORDER_BROKER_SELL, warrantCode);
        if (StringUtils.isNotEmpty(json)) {
            orderBrokerDto = JSON.parseObject(json, OrderBrokerDto.class);
        } else {
            //缓存中没有的时候从数据库中查询
            StockRelatedDetails stockRelatedDetails =
                    stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", warrantCode));
            if (stockRelatedDetails != null) {
                if (BUY.equals(type)) {
                    if (StringUtils.isNotEmpty(stockRelatedDetails.getOrderBrokerBuyDetails())) {
                        orderBrokerDto = JSON.parseObject(stockRelatedDetails.getOrderBrokerBuyDetails(), OrderBrokerDto.class);
                    }
                } else {
                    if (StringUtils.isNotEmpty(stockRelatedDetails.getOrderBrokerSellDetails())) {
                        orderBrokerDto = JSON.parseObject(stockRelatedDetails.getOrderBrokerSellDetails(), OrderBrokerDto.class);
                    }
                }
            }
        }
        return ResultT.success(orderBrokerDto);
    }

    @Override
    public ResultT<Order> getOrder(String warrantCode) {
        Order order = new Order();
        String json = redisClient.hget(RedisKeyConstants.RECEIVER_WARRANT_MAP_ORDER, warrantCode);
        if (StringUtils.isNotEmpty(json)) {
            order = JSON.parseObject(json, Order.class);
        } else {
            StockRelatedDetails stockRelatedDetails =
                    stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", warrantCode));
            if (stockRelatedDetails != null && StringUtils.isNotEmpty(stockRelatedDetails.getOrderDetails())) {
                order = JSON.parseObject(stockRelatedDetails.getOrderDetails(), Order.class);
            }
        }
        return ResultT.success(order);
    }

    @Override
    public ResultT<List<WarrantCodeVo>> getWarrantCodeList(String sort, String sortKey) {
        return ResultT.success(warrantService.getWarrantCodeList(sort, sortKey));
    }

    @Override
    public ResultT<List<WarrantSnapshot>> getWarrantSnapshotList(String[] warrantCodes) {
        List<WarrantSnapshot> snapshotList = new LinkedList<>();
        Arrays.asList(warrantCodes.clone()).forEach(s -> {
            String json = redisClient.hget(RedisKeyConstants.RECEIVER_WARRANT_MAP_STOCK_SNAPSHOT, s);
            if (StringUtils.isNotEmpty(json)) {
                WarrantSnapshot warrantSnapshot = JSON.parseObject(json, WarrantSnapshot.class);
                snapshotList.add(warrantSnapshot);
            } else {
                StockRelatedDetails stockRelatedDetails =
                        stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", s));
                if (stockRelatedDetails != null && StringUtils.isNotEmpty(stockRelatedDetails.getSnapshotDetails())) {
                    WarrantSnapshot warrantSnapshot = JSON.parseObject(stockRelatedDetails.getSnapshotDetails(), WarrantSnapshot.class);
                    snapshotList.add(warrantSnapshot);
                }
            }
        });
        return ResultT.success(snapshotList);
    }

    @Override
    public ResultT<List<DDENetVo>> getDdeNetList(String warrantCode) {
        List<DDENetVo> voList = new ArrayList<>();
        // 查询所有资金分布
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listSixtyRealTime(warrantCode, tradingDay, Boolean.TRUE);
        if (CollectionUtils.isNotEmpty(tradeStatistics)) {
            voList = tradeStatistics.stream().map(dto -> {
                DDENetVo ddeNetVo = new DDENetVo();
                ddeNetVo.setDate(dto.getTime());
                // 主力资金包含特大单, 大单
                BigDecimal add = dto.getInCapitalXLarge().add(dto.getInCapitalLarge()).
                        subtract(dto.getOutCapitalLarge()).subtract(dto.getOutCapitalXLarge());
                ddeNetVo.setNetInflow(add);
                return ddeNetVo;
            }).collect(Collectors.toList());
        }
        return ResultT.success(voList);
    }

    @Override
    public ResultT<List<NetRankVo>> getNetRank(String warrantCode) {
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

        Map<String, TodayCapitalTotalDto> todayMap = tradeStatisticsApi.getAllTodayTotalAmountFromRedis(date,Boolean.TRUE);
        if (MapUtil.isEmpty(todayMap)) {
            return ResultT.success(Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo));
        }
        // 是否展示今日的数据 1.交易日的非盘前竞价;2.非交易日的任意时刻
        boolean addToday = !DateUtils.bidding() || !tradingDay;
        TodayCapitalTotalDto todayCapitalTotalDto = todayMap.getOrDefault(warrantCode, null);
        if (addToday && todayCapitalTotalDto != null) {
            long todayRank = todayMap.values().stream().filter(t -> t.getNet().compareTo(todayCapitalTotalDto.getNet()) > 0).count() + 1;
            todayVo.setRank((int) todayRank);
            todayVo.setNetInflow(todayCapitalTotalDto.getNet());
        }

        Map<String, TotalCapitalInflowsDTO> totalMap = tradeStatisticsApi.getTotalCapitalInflowsFromRedis(date, Boolean.TRUE);
        if (MapUtil.isEmpty(totalMap)) {
            return ResultT.success(Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo));
        }
        TotalCapitalInflowsDTO totalCapitalInflowsDTO = totalMap.getOrDefault(warrantCode, null);
        if (totalCapitalInflowsDTO == null) {
            return ResultT.success(Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo));
        }
        if (addToday) {
            totalMap.values().forEach(total -> {
                TodayCapitalTotalDto todayCapitalTotal = todayMap.getOrDefault(total.getStockCode(), null);
                if (ObjectUtils.isEmpty(todayCapitalTotal)) {
                    return;
                }
                total.setFiveDaysTotal(total.getFiveDaysTotal().add(todayCapitalTotal.getNet()));
                total.setTenDaysTotal(total.getTenDaysTotal().add(todayCapitalTotal.getNet()));
                total.setTwentyDaysTotal(total.getTwentyDaysTotal().add(todayCapitalTotal.getNet()));
                total.setSixtyDaysTotal(total.getSixtyDaysTotal().add(todayCapitalTotal.getNet()));
            });
        }

        long fiveDaysRank = totalMap.values().stream().filter(t -> t.getFiveDaysTotal().compareTo(totalCapitalInflowsDTO.getFiveDaysTotal()) > 0).count() + 1;
        fiveVo.setRank((int) fiveDaysRank);
        fiveVo.setNetInflow(totalCapitalInflowsDTO.getFiveDaysTotal());

        long tenDaysRank = totalMap.values().stream().filter(t -> t.getTenDaysTotal().compareTo(totalCapitalInflowsDTO.getTenDaysTotal()) > 0).count() + 1;
        tenVo.setRank((int) tenDaysRank);
        tenVo.setNetInflow(totalCapitalInflowsDTO.getTenDaysTotal());

        long twentyDaysRank = totalMap.values().stream().filter(t -> t.getTwentyDaysTotal().compareTo(totalCapitalInflowsDTO.getTwentyDaysTotal()) > 0).count() + 1;
        twentyVo.setRank((int) twentyDaysRank);
        twentyVo.setNetInflow(totalCapitalInflowsDTO.getTwentyDaysTotal());

        long sixtyDaysRank = totalMap.values().stream().filter(t -> t.getSixtyDaysTotal().compareTo(totalCapitalInflowsDTO.getSixtyDaysTotal()) > 0).count() + 1;
        sixtyVo.setRank((int) sixtyDaysRank);
        sixtyVo.setNetInflow(totalCapitalInflowsDTO.getSixtyDaysTotal());
        return ResultT.success(Arrays.asList(todayVo, fiveVo, tenVo, twentyVo, sixtyVo));
    }

    @Override
    public ResultT<CapitalDistributionVo> getWarrantCapitalDistribution(String stockCode) {
        CapitalDistributionVo resultVo = new CapitalDistributionVo();
        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        List<TradeStatistics> tradeStatistics = tradeStatisticsApi.listSixtyRealTime(stockCode, tradingDay, Boolean.TRUE);
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
        return ResultT.success(resultVo);
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

    /*@Override
    public ResultT<CapitalDistributionVo> getWarrantCapitalDistributionOld(String warrantCode) {
        // 查询所有资金分布
        CapitalDistributionVo resultVo = new CapitalDistributionVo();
        List<Object> objects = redisClient.lGet(RedisKeyConstants.TRADE_WARRANT_LIST_CAPITAL_DISTRIBUTION + warrantCode, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            log.info("权证代码:" + warrantCode + " 查询不到资金分布，请检查！！！！！");
            return ResultT.success(resultVo);
        }
        Long time = ((TradeStatisticsDto) objects.get(0)).getTime();
        // 交易日的盘前竞价
        boolean tradingDay = tradingCalendarApi.isTradingDay(LocalDate.now());
        LocalDate date = LocalDate.now();
        if (!tradingDay) {
            date = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
        }
        long nineClock = DateUtils.getNineClock(date);
        boolean tradingDayBidding = DateUtils.bidding() && tradingDay;
        // 盘前竞价有数据
        boolean hasBiddingDate = time > nineClock;

        List<CapitalDistributionDTO> dtoList = new ArrayList<>();
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
            } else if (size == 1 && hasBiddingDate) {
                // 有且只有一条盘前竞价的数据
                dto = new CapitalDistributionDTO();
            } else if (size == 1) {
                // 仅有前一天的数据
                dto = buildCapitalDistributionDTO((TradeStatisticsDto) objects.get(0));
            } else if (hasBiddingDate) {
                // 盘前竞价有数据 需要过滤掉盘前竞价的数据
                dto = objects.subList(1, Math.min(size, value.getDays())).stream().map(c ->
                        buildCapitalDistributionDTO((TradeStatisticsDto) c)).reduce(CapitalDistributionDTO::add).get();
            } else {
                // 交易日盘前竞价没有数据
                dto = objects.subList(0, Math.min(size, value.getDays()) - 1).stream().map(c ->
                        buildCapitalDistributionDTO((TradeStatisticsDto) c)).reduce(CapitalDistributionDTO::add).get();
            }
            dto.setCode(warrantCode);
            dto.setType(value.getCode());
            dtoList.add(dto);
        }
        resultVo.setCapitalDistributionList(dtoList);
        resultVo.setTime(tradingDayBidding ? nineClock : time);
        return ResultT.success(resultVo);
    }*/

    @Override
    public ResultT<VolumeStatisticsDTO> getWarrantDealStatistics(String warrantCode, Long date, String direction) {
        VolumeStatisticsDTO volumeStatisticsDTO = new VolumeStatisticsDTO();
        LocalDate localDate = date == null ? LocalDate.now() : LocalDateTimeUtil.getLocalDate(date, ZoneOffset.of("+8"));
        // 是否查询今天数据
        boolean isToday = localDate.compareTo(LocalDate.now()) >= 0;
        // 是否交易日
        boolean tradingDay = tradingCalendarApi.isTradingDay(localDate);
        if (!tradingDay) {
            // 非交易日查询上一个交易日的数据
            localDate = tradingCalendarApi.getBeforeTradingCalendar(localDate).getDate();
        } else if (isToday && DateUtils.beforeNineHour()) {
            // 九点前查询前一天
            localDate = tradingCalendarApi.getBeforeTradingCalendar(localDate).getDate();
        } else if (isToday && DateUtils.bidding()) {
            // 盘前竞价返回空数据
            return ResultT.success(volumeStatisticsDTO);
        }
        volumeStatisticsDTO = redisClient.hget(RedisKeyConstants.TRADE_WARRANT_STOCK_DEAL_STATISTICS_MAP.concat(localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))), warrantCode);
        if (volumeStatisticsDTO == null) {
            volumeStatisticsDTO = new VolumeStatisticsDTO();
            volumeStatisticsDTO.init();
            log.info("股票代码:" + warrantCode + " 查询不到成交统计，请检查！！！！！");
        }
        BigDecimal totalVolume = volumeStatisticsDTO.getTotalVolume();
        List<DealStatisticsDTO> dealList = volumeStatisticsDTO.getDealList();

        if (StringUtils.isNotBlank(direction) && TradeDirectionConstants.IN.equals(direction)) {
            dealList = dealList.stream().filter(item -> item.getInVolume().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(direction) && TradeDirectionConstants.OUT.equals(direction)) {
            dealList = dealList.stream().filter(item -> item.getOutVolume().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(direction) && TradeDirectionConstants.NEUTER.equals(direction)) {
            dealList = dealList.stream().filter(item -> item.getNeuterVolume().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        }
        dealList.forEach(item -> item.setRatio(item.getTotalVolume().divide(totalVolume, 6, RoundingMode.HALF_UP)));
        dealList.sort(Comparator.comparing(DealStatisticsDTO::getPrice).reversed());
        volumeStatisticsDTO.setTime(System.currentTimeMillis());
        volumeStatisticsDTO.setDealList(dealList);
        return ResultT.success(volumeStatisticsDTO);
    }
}
