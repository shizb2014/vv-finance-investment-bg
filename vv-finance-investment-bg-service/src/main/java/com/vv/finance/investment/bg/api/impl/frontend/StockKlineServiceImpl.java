package com.vv.finance.investment.bg.api.impl.frontend;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vv.finance.common.constants.kline.EventConstants;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.v2.StockServiceV2;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.dto.info.DealDTO;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/11/9 16:10
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}",registry = "bgservice")
@Slf4j

public class StockKlineServiceImpl extends AbstractBaseServiceImpl implements IStockKlineService {
    @Autowired
    Xnhk0127Mapper xnhk0127Mapper;
    @Autowired
    F10KeyFiguresDao f10KeyFiguresDao;
    @Autowired
    private  StockServiceV2 stockServiceV2;
    @Autowired
    private  ExecutorService klineExecutorService;
    @Resource
    private HkTradingCalendarApi tradingCalendarApi;
    @Resource
    private IStockMarketService stockMarketService;
    @Resource
    Xnhk0008Mapper xnhk0008Mapper;
    @Resource
    Xnhk0201Mapper xnhk0201Mapper;
    @Resource
    Xnhk0204Mapper xnhk0204Mapper;
    @Resource
    Xnhk0207Mapper xnhk0207Mapper;
    @Resource
    Xnhk0307Mapper xnhk0307Mapper;

    private static final Set<String> IGNORE_REPORT= Sets.newHashSet("P","Q5");
    @Value("${vv.mock.point:off}")
    private String mockBuyPoint;

    @Autowired
    private StockCache stockCache;

    private static final String MOCK_ON="on";
    private static final int MOCK_NUM=1;
    private static final LocalTime startTime = LocalTime.of(9,0,0);
    @Value("${vv.mock.rate:10}")
    private   Integer mockRate;


    @Autowired
    private StockInfoApi stockInfoApi;



    @Override
    public List<BaseKlineDTO> queryAndSetEvent(String code, String type, String adjhkt, List<BaseKlineDTO> klineList){
        if(CollUtil.isEmpty(klineList)|| type.startsWith("min")){
            return klineList;
        }
        try {
            //除权事件
            Map<Long, List<EventDTO>> dividendEvent = dividendEvent(code, type);
            //财报事件 0 =非金融 1=金融 2=保险
            int marketType = getMarketType(code);
            Map<Long, List<EventDTO>> finEvent;
            List<Xnhk0008> xnhk0008s = xnhk0008Mapper.selectList(null);
            Map<String, String> currencyMap = xnhk0008s.stream().collect(Collectors.toMap(Xnhk0008::getCode, Xnhk0008::getF001v));
            switch (marketType){
                case 0:
                    finEvent=noFinEvent(code, type,currencyMap);
                    break;
                case 1:
                    finEvent=finEvent(code, type,currencyMap);
                    break;
                case 2:
                    finEvent=insEvent(code, type,currencyMap);
                    break;
                default :
                    finEvent=Maps.newHashMap();
            }
            //获取股票转板信息
            List<Xnhk0307> xnhk0307s = xnhk0307Mapper.findAllConversionMarketInfo(code, com.vv.finance.common.utils.DateUtils.formatDate(new Date(),"yyyyMMdd"));
            Map<Long, List<EventDTO>> conversionMarketMap = xnhk0307s.stream().map(xnhk0307 -> {
                EventDTO eventDTO = new EventDTO();
                eventDTO.setTime(com.vv.finance.common.utils.DateUtils.parseDate(xnhk0307.getF003d().toString()).getTime());
                eventDTO.setSourceExchange("创业板");
                eventDTO.setTargetExchange("主板");
                eventDTO.setEventType(EventConstants.CM);
                return eventDTO;
            }).collect(Collectors.groupingBy(item -> convertTime(type, LocalDateTimeUtil.getLocalDate(item.getTime()))));
            log.info("获取转板事件 conversionMarketMap：{}",conversionMarketMap);
            klineList.forEach(klineDTO -> {
                if(MOCK_ON.equalsIgnoreCase(mockBuyPoint)&&"day".equalsIgnoreCase(type)
                        &&"not".equalsIgnoreCase(adjhkt)){
                    int i = RandomUtil.randomInt(0, mockRate);
                    if(i==MOCK_NUM){
                        setMockBuyPointTimeChart(klineDTO,false);
                    }

                }
                List<EventDTO> dividends= dividendEvent.get(klineDTO.getTime());
                List<EventDTO> finEvents = finEvent.get(klineDTO.getTime());
                List<EventDTO> conversionMarketEvents = conversionMarketMap.get(klineDTO.getTime());
                List<EventDTO> event =new ArrayList<>();
                if(CollUtil.isNotEmpty(dividends)){
                    event.addAll(dividends);
                }
                if(CollUtil.isNotEmpty(finEvents)){
                    event.addAll(finEvents);
                }
                if(CollUtil.isNotEmpty(conversionMarketEvents)){
                    event.addAll(conversionMarketEvents);
                }
                klineDTO.setEvent(event);
            });
        } catch (Exception e) {
            log.error("",e);
        }
        return klineList;
    }

    private Map<Long,List<EventDTO>> finEvent(String code, String type,Map<String, String> currencyMap){
        F10PageResp<F10KeyFiguresFinancialEntity> resp = f10KeyFiguresDao.pageFinancial(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresFinancialEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Maps.newHashMap();
        }
        //过滤掉F005V为Y的数据
        List<Xnhk0204> filterData = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().select("SECCODE","F002D","F005V","F006V").eq("SECCODE", code).eq("F005V","Y"));
        Map<String, Xnhk0204> filterDataMap = filterData.stream().collect(Collectors.toMap(item -> item.getF002d().toString().concat("-").concat(item.getF006v()), Function.identity()));
        Predicate<F10KeyFiguresFinancialEntity> predicate = item -> !IGNORE_REPORT.contains(item.getReportType())
                && filterDataMap.get(item.getEndDate().replace("/", "").concat("-").concat(item.getReportType())) == null;
        //处理财报重复问题
        record.sort(Comparator.comparing(F10KeyFiguresFinancialEntity::getEndTimestamp).reversed());
        Map<String, F10KeyFiguresFinancialEntity> recordMap = record.stream().filter(predicate).collect(Collectors.toMap(o -> o.getStockCode().concat(o.getReportType()).concat(o.getReleaseDate()), o -> o, (k1, k2) -> k1));
        record= new ArrayList<>( recordMap.values());

        return record.stream().filter(predicate).map(item -> {
            EventDTO eventDTO= new EventDTO();
            eventDTO.setTime(item.getReleaseTimestamp());
            eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
            eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
            eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
            String currencyDesc = currencyMap.get(item.getCurrency());
            eventDTO.setCurrencyDesc(StringUtils.isEmpty(currencyDesc) ? item.getCurrency() : currencyDesc);
            return eventDTO;
        }).collect(Collectors.groupingBy(item->convertTime(type,LocalDateTimeUtil.getLocalDate(item.getTime()))));


    }

    private Map<Long,List<EventDTO>> noFinEvent(String code, String type,Map<String, String> currencyMap){
        F10PageResp<F10KeyFiguresNonFinancialEntity> resp = f10KeyFiguresDao.pageNonFinancial(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresNonFinancialEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Maps.newHashMap();
        }
        //过滤掉F005V为Y的数据
        List<Xnhk0201> filterData = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().select("SECCODE","F002D","F005V","F006V").eq("SECCODE", code).eq("F005V","Y"));
        Map<String, Xnhk0201> filterDataMap = filterData.stream().collect(Collectors.toMap(item -> item.getF002d().toString().concat("-").concat(item.getF006v()), Function.identity()));
        Predicate<F10KeyFiguresNonFinancialEntity> predicate = item -> !IGNORE_REPORT.contains(item.getReportType())
                && (Objects.nonNull(item.getKeyFigures().getNetProfits()) && Objects.nonNull(item.getKeyFigures().getNetProfits()))
                && filterDataMap.get(item.getEndDate().replace("/","").concat("-").concat(item.getReportType())) == null;
        //处理财报重复问题
        record.sort(Comparator.comparing(F10KeyFiguresNonFinancialEntity::getEndTimestamp).reversed());
        Map<String, F10KeyFiguresNonFinancialEntity> recordMap = record.stream().filter(predicate).collect(Collectors.toMap(o -> o.getStockCode().concat(o.getReportType()).concat(o.getReleaseDate()), o -> o, (k1, k2) -> k1));
        record= new ArrayList<>( recordMap.values());

        return record.stream().
                filter(predicate).
                map(item -> {
            EventDTO eventDTO= new EventDTO();
            eventDTO.setTime(item.getReleaseTimestamp());
            eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
            eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
            eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
            String currencyDesc = currencyMap.get(item.getCurrency());
            eventDTO.setCurrencyDesc(StringUtils.isEmpty(currencyDesc) ? item.getCurrency() : currencyDesc);
            return eventDTO;
        }).collect(Collectors.groupingBy(item->convertTime(type,LocalDateTimeUtil.getLocalDate(item.getTime()))));


    }
    private Map<Long,List<EventDTO>> insEvent(String code, String type,Map<String, String> currencyMap){
        F10PageResp<F10KeyFiguresInsuranceEntity> resp = f10KeyFiguresDao.pageInsurance(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresInsuranceEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Maps.newHashMap();
        }
        //过滤掉F005V为Y的数据
        List<Xnhk0207> filterData = xnhk0207Mapper.selectList(new QueryWrapper<Xnhk0207>().select("SECCODE","F002D","F005V","F006V").eq("SECCODE", code).eq("F005V","Y"));
        Map<String, Xnhk0207> filterDataMap = filterData.stream().collect(Collectors.toMap(item -> item.getF002d().toString().concat("-").concat(item.getF006v()), Function.identity()));
        Predicate<F10KeyFiguresInsuranceEntity> predicate = item -> !IGNORE_REPORT.contains(item.getReportType())
                && filterDataMap.get(item.getEndDate().replace("/", "").concat("-").concat(item.getReportType())) == null;
        //处理财报重复问题
        record.sort(Comparator.comparing(F10KeyFiguresInsuranceEntity::getEndTimestamp).reversed());
        Map<String, F10KeyFiguresInsuranceEntity> recordMap = record.stream().filter(predicate).collect(Collectors.toMap(o -> o.getStockCode().concat(o.getReportType()).concat(o.getReleaseDate()), o -> o, (k1, k2) -> k1));
        record= new ArrayList<>( recordMap.values());

        return record.stream().filter(predicate).map(item -> {
            EventDTO eventDTO= new EventDTO();
            eventDTO.setTime(item.getReleaseTimestamp());
            eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
            eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
            eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
            String currencyDesc = currencyMap.get(item.getCurrency());
            eventDTO.setCurrencyDesc(StringUtils.isEmpty(currencyDesc) ? item.getCurrency() : currencyDesc);
            return eventDTO;
        }).collect(Collectors.groupingBy(item->convertTime(type,LocalDateTimeUtil.getLocalDate(item.getTime()))));


    }
    private  Map<Long,List<EventDTO>> dividendEvent(String code,String type){
        List<Xnhk0127> xnhk0127ByCode = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>().eq("SECCODE",code)
        .ne("F007N",0));
        String klineType=type;

        if (CollUtil.isNotEmpty(xnhk0127ByCode)) {
            return xnhk0127ByCode.stream().filter(item -> item.getF003d() != null)
                    .map(xnhk0127 -> {
                       EventDTO eventDTO = new EventDTO();
                       eventDTO.setTime(LocalDateTimeUtil.getTimestamp(xnhk0127.getF003d(), ZoneOffset.ofHours(8)));
                       eventDTO.setExContent(xnhk0127.getF006v());
                        String f002v = xnhk0127.getF002v().split(",")[0];
                        eventDTO.setEventType(EventConstants.EVENT_RELATION.get(f002v));
                       return eventDTO;
                    }).collect(Collectors.groupingBy(item ->convertTime(klineType,LocalDateTimeUtil.getLocalDate(item.getTime()))));

        }

        return Maps.newHashMap();
    }

    private long convertTime(String type, LocalDate time){
        switch (type){
            case "week":
                return LocalDateTimeUtil
                        .getTimestamp(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
            case "month":
                return LocalDateTimeUtil
                        .getTimestamp(time.withDayOfMonth(1));

            case "quarter":
                return LocalDateTimeUtil
                        .getTimestamp(LocalDateTimeUtil.getStartDayOfQuarter(time));

            case "year":
                return LocalDateTimeUtil
                        .getTimestamp(time.with(TemporalAdjusters.firstDayOfYear()));
            default :
                return LocalDateTimeUtil.getTimestamp(time);

        }
    }


    private LocalDateTime pointToTime(LocalDate date,Integer point){
        String pointStr=point.toString();
        int hourSize=pointStr.length()==4?2:1;
        String hour=pointStr.substring(0,hourSize);
        String minute = pointStr.substring(hourSize);
        LocalTime time=LocalTime.of(Integer.parseInt(hour),Integer.parseInt(minute));
        return LocalDateTime.of(date, time);
    }

    private void setMockBuyPointTimeChart(BaseKlineDTO baseKlineDTO,boolean timeChar){

        List<DealDTO> dealList = Lists.newArrayList();
        int count = RandomUtil.randomInt(1, 6);
        for (int i = 0; i < count; i++) {
            DealDTO dealDTO = new DealDTO();
            dealDTO.setTradeType(RandomUtil.randomInt(0, 2));
            dealDTO.setDirect(RandomUtil.randomInt(0, 2));
            if (timeChar) {
                dealDTO.setTime(baseKlineDTO.getTime());
            } else {
                LocalDate date = LocalDateTimeUtil.getLocalDate(baseKlineDTO.getTime());
                int am = RandomUtil.randomInt(0, 2);
                LocalTime time;
                if (0 == am) {
                    time = LocalTime.of(RandomUtil.randomInt(10, 12), RandomUtil.randomInt(0, 60));
                } else {
                    time = LocalTime.of(RandomUtil.randomInt(13, 16), RandomUtil.randomInt(0, 60));
                }
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                if (dateTime.isAfter(LocalDateTime.now())) {
                    dateTime = LocalDateTime.of(date, LocalTime.of(9, 31));
                }
                dealDTO.setTime(LocalDateTimeUtil.getTimestamp(dateTime));
            }
            BigDecimal price = baseKlineDTO.getClose();
            if (baseKlineDTO.getLow().compareTo(baseKlineDTO.getHigh()) < 0) {
                price = RandomUtil.randomBigDecimal(baseKlineDTO.getLow(), baseKlineDTO.getHigh());
            }
            dealDTO.setPrice(price);
            dealDTO.setNum(RandomUtil.randomInt(1000, 100000));
            dealList.add(dealDTO);

        }
        baseKlineDTO.setDealList(dealList);

    }

}
