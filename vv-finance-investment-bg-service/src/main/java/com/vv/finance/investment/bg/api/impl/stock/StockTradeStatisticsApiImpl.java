package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.GlobalConstants;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.JsonUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.info.TodayCapitalTotalDto;
import com.vv.finance.investment.bg.dto.info.TotalCapitalInflowsDTO;
import com.vv.finance.investment.bg.dto.info.TradeStatisticsDto;
import com.vv.finance.investment.bg.dto.info.VolumeStatisticsDTO;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;
import com.vv.finance.investment.bg.stock.trade.mapper.StockTradeStatisticsMapper;
import com.vv.finance.investment.bg.stock.trade.service.impl.StockTradeStatisticsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author wsl_vv
 * @Deacription 资金分布对外接口
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class StockTradeStatisticsApiImpl implements StockTradeStatisticsApi {

    @Resource
    private StockTradeStatisticsServiceImpl tradeStatisticsService;

    @Resource
    private StockTradeStatisticsMapper tradeStatisticsMapper;

    @Resource
    private HkTradingCalendarApi tradingCalendarApi;

    @Resource
    RedisClient redisClient;

//    @DubboReference(group = "${dubbo.investment.trade.service.group:trade}", registry = "tradeservice")
//    private StockTradeApi stockTradeApi;
//
//    @DubboReference(group = "${dubbo.investment.trade.service.group:trade}", registry = "tradeservice")
//    private TradeService tradeService;

    private static final int SIXTY = 60;

    //资金失效时间 7天
    private static final long expireTime = 60 * 60 * 24 * 10;

    @Override
    public Boolean saveBatch(List<TradeStatisticsDto> tradeStatisticsDtos) {
        List<TradeStatistics> tradeStatistics = new ArrayList<>();
        for (TradeStatisticsDto dto : tradeStatisticsDtos) {
            tradeStatistics.add(buildFromDto(dto));
        }
        boolean b = tradeStatisticsService.saveBatch(tradeStatistics);
        if (b) {
            log.info("资金分布数据落库成功！");
        } else {
            log.info("资金分布数据落库失败！");
        }
        return b;
    }

    @Override
    public Boolean saveBatchV2(List<TradeStatistics> tradeStatistics) {
        log.info("资金分布数据落库:{}", tradeStatistics.size());
        tradeStatisticsMapper.batchSaveOrUpdate(tradeStatistics);
        return true;
    }

    @Override
    public List<TradeStatistics> listSixty(String stockCode) {

        ResultT<List<BgTradingCalendar>> lastTradingCalendars = tradingCalendarApi.getLastTradingCalendarsLtDate(LocalDate.now(), SIXTY);
        if (ResultCode.SUCCESS.code() != lastTradingCalendars.getCode()) {
            log.info("交易日历获取失败！");
            return Lists.newArrayList();
        }
        // 60日数据补齐
        List<BgTradingCalendar> bgTradingCalendars = lastTradingCalendars.getData();
        long startTime = bgTradingCalendars.get(bgTradingCalendars.size()-1).getDate().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endTime = bgTradingCalendars.get(0).getDate().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()+24*60*60*1000-1;
        List<TradeStatistics> tradeStatistics = tradeStatisticsMapper.selectList(new QueryWrapper<TradeStatistics>().eq(TradeStatistics.COL_STOCK_CODE, stockCode).between(true,TradeStatistics.COL_TIME,startTime,endTime).orderByDesc(TradeStatistics.COL_TIME));

        TradeStatistics defaultData;
        for (int i = 0; i < bgTradingCalendars.size(); i++) {
            Long timestamp = LocalDateTimeUtil.getTimestamp(bgTradingCalendars.get(i).getDate());
            if (tradeStatistics.size() <= i || timestamp.compareTo(tradeStatistics.get(i).getTime()) > 0) {
                defaultData = new TradeStatistics();
                defaultData.init(DateUtils.getNineClock(bgTradingCalendars.get(i).getDate()), stockCode);
                tradeStatistics.add(i, defaultData);
            }
        }
        return tradeStatistics;
    }


    @Override
    public List<TradeStatistics> listTradeStatisticsRealTime(String stockCode, boolean isTradingDay, Boolean isWarrant, LocalDate date, Integer todayFlag) {
        List<TradeStatistics> tradeStatistics = new ArrayList<>();
//        if(todayFlag != GlobalConstants.FLAG_YES){
            //查询历史数据
            tradeStatistics = listTradeStatistics(stockCode, date);
//        }
//        Integer tradingDaysCount = tradingCalendarApi.getBetweenTradingDays(date, LocalDate.now()).getData();
        if (isTradingDay) {
            if (LocalTime.now().isBefore(LocalTime.of(9, 0))) {
                // 九点前
                return tradeStatistics;
            }
            TradeStatisticsDto dto = redisClient.hget((isWarrant ? RedisKeyConstants.TRADE_MAP_WARRANT_CAPITAL_DISTRIBUTION : RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION) + LocalDate.now(), stockCode);
            Long updateTime = tradeStatistics.size() == 0 ? 0 : tradeStatistics.get(0).getTime();
            TradeStatisticsDto todayStatistics;
            if (DateUtils.bidding() || dto == null) {
                // 盘前竞价 或者今天没有交易
                todayStatistics = new TradeStatisticsDto();
                todayStatistics.setTime(DateUtils.getNineClock(LocalDate.now()));
            } else {
                // 盘后
                todayStatistics = dto;
            }
            if (updateTime < todayStatistics.getTime()) {
                tradeStatistics.add(0, buildFromDto(todayStatistics));
            }
            //返回数据
//            if (tradeStatistics.size() > tradingDaysCount){
//                tradeStatistics.remove(tradeStatistics.size()-1);
//            }
        }
        return tradeStatistics;
    }

    @Override
    public List<TradeStatistics> listTradeStatistics(String stockCode, LocalDate date) {

        List<BgTradingCalendar> bgTradingCalendars = tradingCalendarApi.getTradingCalendarByDateNoToday(date, LocalDate.now());
        if (CollectionUtils.isEmpty(bgTradingCalendars)) {
            log.info("交易日历获取失败！");
            return Lists.newArrayList();
        }
        // 交易日日数据补齐
//        List<BgTradingCalendar> bgTradingCalendars = lastTradingCalendars.getData();
        long startTime = bgTradingCalendars.get(bgTradingCalendars.size()-1).getDate().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endTime = bgTradingCalendars.get(0).getDate().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()+24*60*60*1000-1;
        List<TradeStatistics> tradeStatistics = tradeStatisticsMapper.selectList(new QueryWrapper<TradeStatistics>().eq(TradeStatistics.COL_STOCK_CODE, stockCode).between(true,TradeStatistics.COL_TIME,startTime,endTime).orderByDesc(TradeStatistics.COL_TIME));

        TradeStatistics defaultData;
        for (int i = 0; i < bgTradingCalendars.size(); i++) {
            Long timestamp = LocalDateTimeUtil.getTimestamp(bgTradingCalendars.get(i).getDate());
            if (tradeStatistics.size() <= i || timestamp.compareTo(tradeStatistics.get(i).getTime()) > 0) {
                defaultData = new TradeStatistics();
                defaultData.init(DateUtils.getNineClock(bgTradingCalendars.get(i).getDate()), stockCode);
                tradeStatistics.add(i, defaultData);
            }
        }
        return tradeStatistics;
    }

    @Override
    public List<TradeStatistics> listSixtyRealTime(String stockCode, boolean isTradingDay, Boolean isWarrant) {
        List<TradeStatistics> tradeStatistics = listSixty(stockCode);
        if (isTradingDay) {
            if (DateUtils.beforeNineHour()) {
                // 九点前
                return tradeStatistics;
            }
            TradeStatisticsDto dto = redisClient.hget((isWarrant ? RedisKeyConstants.TRADE_MAP_WARRANT_CAPITAL_DISTRIBUTION : RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION) + LocalDate.now(), stockCode);
            Long updateTime = tradeStatistics.size() == 0 ? 0 : tradeStatistics.get(0).getTime();
            TradeStatisticsDto todayStatistics;
            if (DateUtils.bidding() || dto == null) {
                // 盘前竞价 或者今天没有交易
                todayStatistics = new TradeStatisticsDto();
                todayStatistics.setTime(DateUtils.getNineClock(LocalDate.now()));
            } else {
                // 盘后
                todayStatistics = dto;
            }
            if (updateTime < todayStatistics.getTime()) {
                tradeStatistics.add(0, buildFromDto(todayStatistics));
            }
            //返回60条数据
            if (tradeStatistics.size()>60){
                tradeStatistics.remove(tradeStatistics.size()-1);
            }
        }
        return tradeStatistics;
    }

    @Override
    public List<TradeStatistics> listStaticTradeStatistics(String stockCode, boolean isTradingDay, Long startTime, Long endTime) {
        List<TradeStatistics> tradeStatistics = tradeStatisticsMapper.selectList(new QueryWrapper<TradeStatistics>().eq(TradeStatistics.COL_STOCK_CODE, stockCode).between(true,TradeStatistics.COL_TIME,startTime,endTime).orderByDesc(TradeStatistics.COL_TIME));

        if (isTradingDay) {
            if (DateUtils.beforeNineHour()) {
                // 九点前
                return tradeStatistics;
            }
            TradeStatisticsDto dto = redisClient.hget((false ? RedisKeyConstants.TRADE_MAP_WARRANT_CAPITAL_DISTRIBUTION : RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION) + LocalDate.now(), stockCode);
            Long updateTime = tradeStatistics.size() == 0 ? 0 : tradeStatistics.get(0).getTime();
            TradeStatisticsDto todayStatistics;
            if (DateUtils.bidding() || dto == null) {
                // 盘前竞价 或者今天没有交易
                todayStatistics = new TradeStatisticsDto();
                todayStatistics.setTime(DateUtils.getNineClock(LocalDate.now()));
            } else {
                // 盘后
                todayStatistics = dto;
            }
            // 如果更新时间小于今日数据，则添加今日数据
            if (DateUtils.longToLocalDate(updateTime).isBefore(DateUtils.longToLocalDate(todayStatistics.getTime()))) {
                tradeStatistics.add(0, buildFromDto(todayStatistics));
            }
        }
        return tradeStatistics;
    }

    @Override
    public Map<String, TradeStatisticsDto> getAllTradeStatics(Boolean isWarrant) {
        LocalDate date = LocalDate.now();
        boolean tradingDay = tradingCalendarApi.isTradingDay(date);
        if (DateUtils.beforeNineHour() || !tradingDay) {
            date = tradingCalendarApi.getBeforeTradingCalendar(date).getDate();
        }
        Map<String, TradeStatisticsDto> tradeStatisticsDtoMap = redisClient.hmget((isWarrant ? RedisKeyConstants.TRADE_MAP_WARRANT_CAPITAL_DISTRIBUTION : RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION) + date);
        if (CollectionUtils.isEmptyMap(tradeStatisticsDtoMap)){
            log.info("今日资金分布数据为空！");
        }
        return tradeStatisticsDtoMap;
    }

    @Override
    public TradeStatistics buildFromDto(TradeStatisticsDto dto) {
        TradeStatistics tradeStatistics = new TradeStatistics();
        tradeStatistics.setCreateTime(LocalDateTime.now());
        tradeStatistics.setCode(dto.getCode());
        tradeStatistics.setTime(dto.getTime());
        tradeStatistics.setInCapitalLarge(dto.getTypeIn().getCapitalLarge());
        tradeStatistics.setInCapitalMid(dto.getTypeIn().getCapitalMid());
        tradeStatistics.setInCapitalSmall(dto.getTypeIn().getCapitalSmall());
        tradeStatistics.setInCapitalXLarge(dto.getTypeIn().getCapitalXLarge());
        tradeStatistics.setInCapitalTotal(dto.getTypeIn().getCapitalTotal());

        tradeStatistics.setInQtyLarge(dto.getTypeIn().getQtyLarge());
        tradeStatistics.setInQtyMid(dto.getTypeIn().getQtyMid());
        tradeStatistics.setInQtySmall(dto.getTypeIn().getQtySmall());
        tradeStatistics.setInQtyXLarge(dto.getTypeIn().getQtyXLarge());
        tradeStatistics.setInQtyTotal(dto.getTypeIn().getQtyTotal());

        tradeStatistics.setOutCapitalLarge(dto.getTypeOut().getCapitalLarge());
        tradeStatistics.setOutCapitalMid(dto.getTypeOut().getCapitalMid());
        tradeStatistics.setOutCapitalSmall(dto.getTypeOut().getCapitalSmall());
        tradeStatistics.setOutCapitalXLarge(dto.getTypeOut().getCapitalXLarge());
        tradeStatistics.setOutCapitalTotal(dto.getTypeOut().getCapitalTotal());

        tradeStatistics.setOutQtyLarge(dto.getTypeOut().getQtyLarge());
        tradeStatistics.setOutQtyMid(dto.getTypeOut().getQtyMid());
        tradeStatistics.setOutQtySmall(dto.getTypeOut().getQtySmall());
        tradeStatistics.setOutQtyXLarge(dto.getTypeOut().getQtyXLarge());
        tradeStatistics.setOutQtyTotal(dto.getTypeOut().getQtyTotal());
        return tradeStatistics;
    }

    @Override
    public ResultT<List<TradeStatistics>> listSixty4Dubbo(String stockCode) {
        return ResultT.success(listSixty(stockCode));
    }

    @Override
    public Map<String, TotalCapitalInflowsDTO> getTotalCapitalInflowsFromRedis(LocalDate date, Boolean isWarrant) {
        Map<String, TotalCapitalInflowsDTO> hmget = redisClient.hmget((isWarrant ? RedisKeyConstants.TRADE_WARRANT_MAP_TOTAL_CAPITAL_INFLOWS : RedisKeyConstants.TRADE_MAP_TOTAL_CAPITAL_INFLOWS) + date);
        if (CollectionUtils.isEmptyMap(hmget)) {
            log.info("TRADE_MAP_TOTAL_CAPITAL_INFLOWS is empty!");
        }
        return hmget;
    }

    @Override
    public Map<String, TodayCapitalTotalDto> getTodayTotalAmountFromRedis(String stockCode, LocalDate date, Boolean isWarrant) {
        Map<String, TodayCapitalTotalDto> hmget = redisClient.hget((isWarrant ? RedisKeyConstants.TRADE_WARRANT_MAP_TODAY_TOTAL_AMOUNT : RedisKeyConstants.TRADE_MAP_TODAY_TOTAL_AMOUNT) + date, stockCode);
        if (CollectionUtils.isEmptyMap(hmget)) {
            log.info("TRADE_MAP_TODAY_TOTAL_AMOUNT is empty! stockCode:{}", stockCode);
        }
        return hmget;
    }

    @Override
    public Map<String, TodayCapitalTotalDto> getAllTodayTotalAmountFromRedis(LocalDate date, Boolean isWarrant) {
        Map<String, TodayCapitalTotalDto> hmget = redisClient.hmget((isWarrant ? RedisKeyConstants.TRADE_WARRANT_MAP_TODAY_TOTAL_AMOUNT : RedisKeyConstants.TRADE_MAP_TODAY_TOTAL_AMOUNT) + date);
        if (CollectionUtils.isEmptyMap(hmget)) {
            log.info("TRADE_MAP_TODAY_TOTAL_AMOUNT is empty!");
        }
        return hmget;
    }
    /**
     * 获取热力图排行榜
     *
     * @param num   前几名
     * @param isAsc 是否是正序
     * @return
     */
    @Override
    public String getTradeCapitalNatTop(Integer num, Boolean isAsc) {
        Set<ZSetOperations.TypedTuple<Object>> rangeWithScore =null;
        if (isAsc) {
            rangeWithScore = redisClient.getZSetRangeWithScore(RedisKeyConstants.TRADE_CAPITAL_NET_TOP, 0, num);
        }else {
            rangeWithScore = redisClient.getZSetReverseRangeWithScore(RedisKeyConstants.TRADE_CAPITAL_NET_TOP, 0, num);
        }
        String jsonString = JSON.toJSONString(rangeWithScore);
        log.info("TRADE_CAPITAL_NET_TOP 热力图数据:{}",jsonString);
        return jsonString;
    }

    @Override
    public Map<String, Long> queryPreviousTradeNum(Set<String> stockCodes) {
        Map<String, Long> tradeMap = new ConcurrentHashMap<>();
        List<String> codeList = stockCodes.stream().distinct().collect(Collectors.toList());
        List<Object> objectList = codeList.stream()
                .map(s -> (Object) s.concat("A"))
                .collect(Collectors.toList());
        LocalDate localDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate(); //上个交易日
        String date = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        log.info("获取数据日期：{},对应的codes：{}", date, objectList);
        List<VolumeStatisticsDTO> volumeStatisticsDTO = redisClient.hMultiGet(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat(date), objectList);
        if (codeList.size() == volumeStatisticsDTO.size()) {
            for (int i = 0; i < codeList.size(); i++) {
                if (ObjectUtils.isNotEmpty(volumeStatisticsDTO.get(i))) {
                    tradeMap.put(codeList.get(i), Long.valueOf(volumeStatisticsDTO.get(i).getTotalStrokes()));
                } else {
                    tradeMap.put(codeList.get(i), 0l);
                }
            }
        } else {
            log.info("查询出的数据异常了入参code：{},查询出的数据大小：{}，查询出的具体对象：{}", codeList.size(), volumeStatisticsDTO.size(), JsonUtils.beanToJson(volumeStatisticsDTO));
        }
        return tradeMap;
    }

    @Override
    public List<TradeStatistics> listTradeStatistics(Collection<String> stockCodes, Integer limit) {
        List<TradeStatistics> list = new ArrayList<>(stockCodes.size() * limit);
        for (String stockCode : stockCodes) {
            QueryWrapper<TradeStatistics> wrapper = new QueryWrapper<TradeStatistics>()
                    .select("code,time,in_capital_xlarge,in_capital_large,out_capital_xlarge,out_capital_large")
                    .eq(TradeStatistics.COL_STOCK_CODE, stockCode)
                    .orderByDesc(TradeStatistics.COL_TIME)
                    .last("limit " + limit);
            List<TradeStatistics> tradeStatisticsUses = tradeStatisticsMapper.selectList(wrapper);
            if (CollUtil.isNotEmpty(tradeStatisticsUses)) {
                list.addAll(tradeStatisticsUses);
            }
        }
        return list;
    }

    @Override
    public TradeStatisticsDto listTradeStatisticsByDate(String stockCode, LocalDate date) {
        TradeStatisticsDto dto = redisClient.hget((RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION) + date, stockCode);
        return dto;
    }


    /**
     * 删除临时股票资金数据
     * 1、逐笔统计
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     *
     * @param stockCode
     */
    @Override
    public void delTradeStatisticByStockCode(String stockCode) {
        try {
            LocalDate date = LocalDate.now();
            //处理上一个交易日的数据
            LocalDate localDate = tradingCalendarApi.getBeforeTradingCalendar(date).getDate(); //上个交易日
            log.info("删除股票资金数据：stockCode：{},日期：{}", stockCode, localDate);
            //删除逐笔数据
//            stockTradeApi.delTradeByCode(stockCode);

            //资金分布历史数据
            int flag = tradeStatisticsMapper.delete(new QueryWrapper<TradeStatistics>().eq(TradeStatistics.COL_STOCK_CODE, stockCode));

            //资金分布
            log.info("删除资金分布数据开始：stockCode：{}", stockCode);
            redisClient.hdel(RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION + localDate, stockCode);
            redisClient.hdel(RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION + date, stockCode);

            log.info("变更流入流出数据开始");
            redisClient.hdel(RedisKeyConstants.TRADE_MAP_TOTAL_CAPITAL_INFLOWS + localDate, stockCode);
            redisClient.hdel(RedisKeyConstants.TRADE_MAP_TODAY_TOTAL_AMOUNT + localDate, stockCode);

            redisClient.hdel(RedisKeyConstants.TRADE_MAP_TOTAL_CAPITAL_INFLOWS + date, stockCode);
            redisClient.hdel(RedisKeyConstants.TRADE_MAP_TODAY_TOTAL_AMOUNT + date, stockCode);

            //热力图（股票净资金流入）排行榜
            log.info("删除热力图数据开始：stockCode：{}", stockCode);
            redisClient.delZSetRemove(RedisKeyConstants.TRADE_CAPITAL_NET_TOP, stockCode);

            //成交统计(近五日数据)
            List<BgTradingCalendar> localDates = tradingCalendarApi.getLastTradingCalendars(date, 6).getData(); //上个交易日
            for (BgTradingCalendar bgTradingCalendar : localDates) {
                String format = bgTradingCalendar.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                log.info("删除成交统计数据开始：stockCode：{}，日期：{}", stockCode, format);
                redisClient.hdel(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat(format), stockCode.concat("A"));
                redisClient.hdel(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat(format), stockCode.concat("-"));
                redisClient.hdel(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat(format), stockCode.concat("B"));
                redisClient.hdel(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat(format), stockCode.concat("S"));

            }
        } catch (Exception e) {
            log.info("删除临时股票资金数据code：{} 异常", stockCode, e);
        }
    }

    /**
     * 变更资金数据股票code
     * 1、逐笔
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateTradeStatisticStockCode(String sourceCode, String targetCode) {
        copyTradeStatisticStockCode(sourceCode, targetCode, false);

        delTradeStatisticByStockCode(sourceCode);
    }

    /**
     * 变更资金数据股票code
     * 1、逐笔
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void copyTradeStatisticStockCode(String sourceCode, String targetCode,Boolean mockFlag) {
        LocalDate date = LocalDate.now();
        //处理上一个交易日的数据
        LocalDate localDate = tradingCalendarApi.getBeforeTradingCalendar(date).getDate(); //上个交易日
        if(mockFlag){
            localDate = LocalDate.now();
        }
        this.copyTradeStatistic(sourceCode, targetCode, localDate, mockFlag);

    }

    /**
     * 变更资金数据股票code
     * 1、逐笔
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    public void copyTradeStatistic(String sourceCode, String targetCode, LocalDate localDate, Boolean mockFlag) {
        try {
            log.info("变更资金数据股票code：sourceCode：{} targetCode：{},日期：{}", sourceCode, targetCode, localDate);

            //逐笔数据
            if (mockFlag) {
//                tradeService.updateSimulateTradeByCode(sourceCode, targetCode);
            } else {
//                tradeService.updateTradeByCode(sourceCode, targetCode);
            }

            //资金分布历史数据
            QueryWrapper<TradeStatistics> wrapper = new QueryWrapper<TradeStatistics>()
                    .select("code,time,in_capital_total,in_capital_small,in_capital_mid,in_capital_large,in_capital_xlarge,in_qty_total,in_qty_small,in_qty_mid,in_qty_large,in_qty_xlarge,out_capital_total,out_capital_small,out_capital_mid,out_capital_large,out_capital_xlarge,out_qty_total,out_qty_small,out_qty_mid,out_qty_large,out_qty_xlarge,create_time")
                    .eq(TradeStatistics.COL_STOCK_CODE, sourceCode)
                    .orderByDesc(TradeStatistics.COL_TIME);
            List<TradeStatistics> tradeStatisticsUses = tradeStatisticsMapper.selectList(wrapper);
            List<List<TradeStatistics>> partition = Lists.partition(tradeStatisticsUses, 1000);
            for (List<TradeStatistics> list : partition) {
                list.stream().forEach(item -> item.setCode(targetCode));
                boolean b = tradeStatisticsService.saveBatch(list);
            }

            //资金分布
            log.info("变更资金分布数据开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
            // 1、获取到所有的key
            Set<String> keys = redisClient.getRedisKeys(RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION.concat("*"));
            for (String key : keys) {
                TradeStatisticsDto tradeStatisticsDto = redisClient.hget(key, sourceCode);
                if (ObjectUtils.isNotEmpty(tradeStatisticsDto)) {
                    tradeStatisticsDto.setCode(targetCode);
                    redisClient.hset(key, targetCode, tradeStatisticsDto, expireTime);
                }
            }


            log.info("变更流入流出数据开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
            Set<String> keys1 = redisClient.getRedisKeys(RedisKeyConstants.TRADE_MAP_TOTAL_CAPITAL_INFLOWS.concat("*"));
            for (String key : keys1) {
                TotalCapitalInflowsDTO totalCapitalInflowsDTO = redisClient.hget(key, sourceCode);
                if (ObjectUtils.isNotEmpty(totalCapitalInflowsDTO)) {
                    totalCapitalInflowsDTO.setStockCode(targetCode);
                    redisClient.hset(key, targetCode, totalCapitalInflowsDTO, expireTime);
                }
            }


            Set<String> keys2 = redisClient.getRedisKeys(RedisKeyConstants.TRADE_MAP_TODAY_TOTAL_AMOUNT.concat("*"));
            for (String key : keys2) {
                TodayCapitalTotalDto todayCapitalTotalDto = redisClient.hget(key, sourceCode);
                if (ObjectUtils.isNotEmpty(todayCapitalTotalDto)) {
                    todayCapitalTotalDto.setStockCode(targetCode);
                    redisClient.hset(key, targetCode, todayCapitalTotalDto, expireTime);
                }
            }


            //热力图（股票净资金流入）排行榜
            log.info("变更热力图数据开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
            Double score = redisClient.getScore(RedisKeyConstants.TRADE_CAPITAL_NET_TOP, sourceCode);
            if (ObjectUtils.isNotEmpty(score)) {
                redisClient.zSet(RedisKeyConstants.TRADE_CAPITAL_NET_TOP, targetCode, score);
            }

            Set<String> keys3 = redisClient.getRedisKeys(RedisKeyConstants.TRADE_STOCK_DEAL_STATISTICS_MAP.concat("*"));
            for (String key : keys3) {
                //成交统计(近五日数据)
                log.info("变更成交统计数据开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
                VolumeStatisticsDTO volumeStatisticsDTO = (VolumeStatisticsDTO) redisClient.hget(key, sourceCode.concat("A"));
                if (ObjectUtils.isNotEmpty(volumeStatisticsDTO)) {
                    redisClient.hset(key, targetCode.concat("A"), volumeStatisticsDTO, expireTime);
                }

                VolumeStatisticsDTO volumeStatisticsDTO1 = (VolumeStatisticsDTO) redisClient.hget(key, sourceCode.concat("-"));
                if (ObjectUtils.isNotEmpty(volumeStatisticsDTO1)) {
                    redisClient.hset(key, targetCode.concat("-"), volumeStatisticsDTO1, expireTime);
                }

                VolumeStatisticsDTO volumeStatisticsDTOB = (VolumeStatisticsDTO) redisClient.hget(key, sourceCode.concat("B"));
                if (ObjectUtils.isNotEmpty(volumeStatisticsDTOB)) {
                    redisClient.hset(key, targetCode.concat("B"), volumeStatisticsDTOB, expireTime);
                }

                VolumeStatisticsDTO volumeStatisticsDTOS = (VolumeStatisticsDTO) redisClient.hget(key, sourceCode.concat("S"));
                if (ObjectUtils.isNotEmpty(volumeStatisticsDTOS)) {
                    redisClient.hset(key, targetCode.concat("S"), volumeStatisticsDTOS, expireTime);
                }
            }
        } catch (Exception e) {
            log.info("变更资金数据code：sourceCode：{} targetCode：{} 异常", sourceCode, targetCode, e);
        }

    }

}
