package com.vv.finance.investment.bg.api.impl.broker;

import cn.hutool.core.util.StrUtil;
import com.alibaba.google.common.collect.Lists;
import com.alibaba.google.common.collect.Sets;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.dto.StockIndustry;
import com.vv.finance.investment.bg.entity.uts.Xnhks0104;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0104Mapper;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.bg.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Auther: shizhibiao
 * @Date: 2022/10/28
 * @Description: com.vv.finance.investment.bg.api.impl.broker
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerCommonUtils {

    private final RedisClient redisClient;

    @Autowired
    @Qualifier("brokerNameMapCaffeine")
    Cache<String, String> brokerNameMapCaffeine;

    @Autowired
    @Qualifier("IndustryNameMapCaffeine")
    Cache<String, String> IndustryNameMapCaffeine;

    @Autowired
    @Qualifier("brokerIdMapCaffeine")
    Cache<String, String> brokerIdMapCaffeine;

    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;

    @Resource
    BrokerStatisticsMapper brokerStatisticsMapper;

    @Resource
    Xnhks0104Mapper xnhks0104Mapper;

    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    IStockMarketService stockMarketService;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;


    private static final String INDUSTRY_CACHE="INDUSTRY_CACHE";



    /**
     * 通过四位经纪商ID获取到六位经纪商ID
     *
     * @param brokerId
     * @return
     */
    public String getBrokerId(String brokerId) {
        if (StrUtil.isBlank(brokerId)) {
            log.info("brokerId为空:{}", brokerId);
            return "";
        }
        if(brokerId.equals("9001")){
            return "A00003";
        }
        if(brokerId.equals("9002")){
            return "A00004";
        }

        String brokerIdSix = BrokerConstants.BG_BROKER_ID_RELATION_PROFIT_MAP.get(brokerId);
//        if(StringUtils.isEmpty(brokerIdSix)){
//            brokerIdSix = redisClient.get(BrokerConstants.BG_BROKER_ID_RELATION_PROFIT.concat(brokerId)) == null ? brokerId : redisClient.get(BrokerConstants.BG_BROKER_ID_RELATION_PROFIT.concat(brokerId));
//            brokerIdMapCaffeine.put(brokerId, brokerIdSix);
//        }

        return StrUtil.isBlank(brokerIdSix) ? brokerId : brokerIdSix;
    }

    /**
     * 通过经纪商ID获取经纪商名称
     *
     * @param brokerId
     * @return
     */
    public String getBrokerName(String brokerId) {
        if (StrUtil.isBlank(brokerId)) {
            log.info("brokerId为空:{}", brokerId);
            return "";
        }
        String brokerName = BrokerConstants.BG_BROKER_ID_PROFIT_MAP.get(brokerId);
//        if(StringUtils.isEmpty(brokerName)){
//            brokerName = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT.concat(brokerId)) == null ? "" : redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT.concat(brokerId));
//            brokerNameMapCaffeine.put(brokerId, brokerName);
//        }
        return brokerName == null ? "" : brokerName;
    }



    private final LoadingCache<String, Map<String, String>> stockIndustryDtoLoadingCache= Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(10000)
            .build(k-> industrySubsidiaryService.getStockIndustries(null).stream().
                    collect(Collectors.toMap(StockIndustry::getStockCode, StockIndustry::getIndustryName)));

    public String getBrokerIndustry(String code){
        Map<String,String> industrySubsidiaries = stockIndustryDtoLoadingCache.get(INDUSTRY_CACHE);
        String industryName = industrySubsidiaries.get(code);
        if(StringUtils.isEmpty(industryName)){
            return null;
        }
        return industryName;
    }

    /**
     * 获得经纪商该日期有持仓的股票列表
     * @param brokerId
     * @param date
     * @return
     */
    public Set<String> getHoldingCodeSet(String brokerId, LocalDate date,String industryName, String code){
        Set<String> codeSet = Sets.newHashSet();
        date = hkTradingCalendarApi.isTradingDay(date) ? date : hkTradingCalendarApi.getBeforeTradingCalendar(date).getDate();
        Long f001d = DateUtils.localDateToF001D(date);
        List<BrokerStatistics> stockList = brokerStatisticsMapper.selectCodeList(brokerId, f001d);
        if(date.equals(LocalDate.now())){
            f001d = DateUtils.localDateToF001D(hkTradingCalendarApi.getBeforeTradingCalendar(date).getDate());
            List<BrokerStatistics> stockList1 = brokerStatisticsMapper.selectCodeList(brokerId, f001d);
            stockList.addAll(stockList1);
        }
        //按行业以及股票进行筛选（股票优先）
        if(StringUtils.isNotBlank(code)){
            stockList = stockList.stream().filter(item -> code.equals(item.getSeccode())).collect(Collectors.toList());
        }else {
            stockList = stockList.stream().filter(item -> {
                if (StringUtils.isBlank(industryName)){
                    return true;
                }else {
                    return industryName.equals(item.getF014v());
                }
            }).collect(Collectors.toList());
        }
        //过滤掉系统中没有的股票
//        List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code"));
//        List<String> codeList = stockDefines.stream().map(StockDefine::getCode).collect(Collectors.toList());
        List<String> codeList = stockDefineMapper.selectStockCodeList();
        codeSet = stockList.stream().filter(item -> codeList.contains(item.getSeccode())).map(item -> item.getSeccode()).collect(Collectors.toSet());
        //过滤停牌股票
        List<String> quitCodeList = stockMarketService.getQuitCode().getData();
        codeSet = codeSet.stream().filter(item -> !quitCodeList.contains(item)).collect(Collectors.toSet());
        return codeSet;
    }

    /**
     * 开盘前对日期进行处理
     * 日期为今天且当前时间在开盘前-->返回上一个交易日
     * @param date
     * @return
     */
    public LocalDate processBeforeOpening(LocalDate date){
        return date.equals(LocalDate.now())&&hkTradingCalendarApi.isTradingTimeAM(LocalTime.now()) ? hkTradingCalendarApi.getBeforeTradingCalendar(date).getDate() : date;
    }
}
