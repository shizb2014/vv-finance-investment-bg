package com.vv.finance.investment.bg.job.uts;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.common.constants.MarketDataRedisKeyConstants;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.uts.Xnhks0501;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: CacheDataJob
 * @Description: 缓存数据任务
 * @Author: Demon
 * @Datetime: 2020/12/7   11:27
 */
@Component
public class CacheMarketDataJob {

    //市场数据
    @Resource
    private Xnhk0102Mapper xnhk0102Mapper;

    //新股上市
    @Resource
    private Xnhks0501Mapper xnhks0501Mapper;

    @Resource
    private HkexTdMapper hkexTdMapper;

    //行业
    @Resource
    private Xnhk0004Mapper xnhk0004Mapper;

    //子行业
    @Resource
    private Xnhk0005Mapper xnhk0005Mapper;

    @Resource
    private RedisClient redisUtils;

    //两天的秒数
    private final static long daySecond = 24 * 60 * 60 * 2;

//    /**
//     * 缓存股票市场数据
//     *
//     * @param param
//     * @return
//     */
////    @XxlJob(value = "cacheStockMarketData", author = "李虹良", cron = "0 0/30 9-17 ? * MON-FRI", desc = "缓存股票市场数据")
//    public ReturnT<String> cacheStockMarketData(String param) {
//
//        Map<String, Xnhk0102> xnhk0102Map = xnhk0102Mapper.selectList(new QueryWrapper<>()).stream()
//                .collect(Collectors.toMap(Xnhk0102::getSeccode, Xnhk0102 -> Xnhk0102));
//
//        redisUtils.hmset(MarketDataRedisKeyConstants.MARKET_STOCK, xnhk0102Map);
//
//        return ReturnT.SUCCESS;
//    }

    /**
     * 缓存新股上市的股票
     *
     * @param param
     * @return
     */
    @XxlJob(value = "cacheNewStockData", author = "陈振龙", cron = "0 0 6 ? * 2,3,4,5,6 *", desc = "缓存新股上市的股票")
    public ReturnT<String> cacheNewStockData(String param) {

        long nowDate = Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));

        List<Xnhks0501> xnhks0501List = xnhks0501Mapper.selectList(new QueryWrapper<>());

//        redisUtils.set(MarketDataRedisKeyConstants.ALL_NEW_STOCK_LIST, xnhks0501List.stream().map(Xnhks0501::getSeccode).collect(Collectors.toSet()), daySecond);
        redisUtils.set(MarketDataRedisKeyConstants.NEW_STOCK_LIST, xnhks0501List.stream()
                .filter(xnhks0501 -> xnhks0501.getF002d() == nowDate)
                .map(Xnhks0501::getSeccode)
                .collect(Collectors.toSet()));
        return ReturnT.SUCCESS;
    }

//    /**
//     * 缓存交易日历
//     *
//     * @param param
//     * @return
//     */
////    @XxlJob(value = "cacheTradeCalendarData", author = "李虹良", cron = "0 0 4 ? * MON-FRI ", desc = "缓存交易日历")
//    public ReturnT<String> cacheTradeCalendarData(String param) {
//
//        long nowDate = Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));
//
//        List<HkexTd> hkexTds = hkexTdMapper.selectList(new QueryWrapper<HkexTd>()
//                .gt(HkexTd.COL_RDATE, nowDate)
//                .orderByAsc(HkexTd.COL_RDATE));
//
//        List<TradeCalendar> tradeCalendarList = hkexTds.stream().map(hkexTd -> {
//            TradeCalendar tradeCalendar = new TradeCalendar();
//            tradeCalendar.setDate(hkexTd.getRdate());
//            tradeCalendar.setIsTrade(hkexTd.getIstrade());
//            return tradeCalendar;
//        }).collect(Collectors.toList());
//
//        redisUtils.set(MarketDataRedisKeyConstants.TRADE_CALENDAR, tradeCalendarList);
//        return ReturnT.SUCCESS;
//    }

//    /**
//     * 缓存行业
//     *
//     * @param param
//     * @return
//     */
////    @XxlJob(value = "cacheIndustryData", author = "李虹良", cron = "0 0 3 ? * MON-FRI ", desc = "缓存行业数据")
//    public ReturnT<String> cacheIndustryData(String param) {
//
//        List<Xnhk0004> xnhk0004List = xnhk0004Mapper.selectList(new QueryWrapper<>());
//        List<Xnhk0005> xnhk0005List = xnhk0005Mapper.selectList(new QueryWrapper<>());
//
//        List<IndustryDomain> industryDomainList = xnhk0004List.stream().map(xnhk0004 -> {
//            IndustryDomain industryDomain = new IndustryDomain();
//            industryDomain.setCode(xnhk0004.getCode());
//            industryDomain.setEnName(xnhk0004.getF002v());
//            industryDomain.setZhsName(xnhk0004.getF001v());
//            industryDomain.setZhtName(xnhk0004.getF003v());
//            return industryDomain;
//        }).collect(Collectors.toList());
//
//        industryDomainList.addAll(xnhk0005List.stream().map(xnhk0005 -> {
//            IndustryDomain industryDomain = new IndustryDomain();
//            industryDomain.setCode(xnhk0005.getCode());
//            industryDomain.setEnName(xnhk0005.getF002v());
//            industryDomain.setZhsName(xnhk0005.getF001v());
//            industryDomain.setZhtName(xnhk0005.getF003v());
//            return industryDomain;
//        }).collect(Collectors.toList()));
//
////        redisUtils.set(MarketDataRedisKeyConstants.INDUSTRY_LIST, industryDomainList);
//        redisUtils.hmset(MarketDataRedisKeyConstants.INDUSTRY_MAP, industryDomainList.stream()
//                .collect(Collectors.toMap(IndustryDomain::getCode, industryDomain -> industryDomain)));
//        return ReturnT.SUCCESS;
//    }

}
