package com.vv.finance.investment.bg.api.impl.stock.cache;

import com.fenlibao.security.sdk.ws.core.model.req.KlineReq;
import com.fenlibao.security.sdk.ws.core.model.req.MinuteKReq;
import com.fenlibao.security.sdk.ws.core.model.req.TrendReq;
import com.fenlibao.security.sdk.ws.core.model.resp.TrendResp;
import com.google.common.collect.Lists;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.stock.cache.StockKlineCacheApi;
import com.vv.finance.investment.bg.convert.StockConvert;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.kline.service.StockKlineCacheService;
import com.vv.finance.investment.gateway.api.stock.IKlineBusinessApi;
import com.vv.finance.investment.gateway.dto.req.MinKlineReq;
import com.vv.finance.investment.gateway.dto.resp.AllMaKlineResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/12/10 11:14
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class StockKlineCacheApiImpl implements StockKlineCacheApi {

    private final StockKlineCacheService stockKlineCacheService;



    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice", timeout = 5000)
    protected IKlineBusinessApi klineBusinessApi;

    @Override
    public List<StockKline> dailyList(KlineReq klineReq) {
        Date req=null;
        long day = klineReq.getDay();

        try {
            String  dayStr=day+"";
            if(dayStr.length()>8){
                day=Long.parseLong(DateUtils.formatDate(new Date(day),"yyyyMMdd"));
                klineReq.setDay(day);
            }
             req=DateUtils.parseDate(day +"","yyyyMMdd");
        } catch (ParseException e) {
            log.error("dailyList parse date error",e);
           return Lists.newArrayList();
        }
        List<StockKline> allMaKlineResps= Lists.newArrayList();
        if(  Long.parseLong(DateUtils.formatDate(new Date(),"yyyyMMdd"))== day) {
            allMaKlineResps = stockKlineCacheService.dailyList(klineReq);
            if (allMaKlineResps.isEmpty()) {
                stockKlineCacheService.refreshDaily(klineReq);
                return allMaKlineResps;
                //allMaKlineResps = stockKlineCacheService.dailyList(klineReq);
            }
        }
        if(allMaKlineResps.size()<klineReq.getNumber()) {
            List<StockKline> stockKlines = stockKlineCacheService.queryDailyKlineFixedCache(klineReq);
            allMaKlineResps.addAll(stockKlines);
            Map<Long, StockKline> collect = stockKlines.stream().collect(Collectors.toMap(StockKline::getTime, Function.identity(), (stockKline, stockKline2) -> stockKline));
            allMaKlineResps=Lists.newArrayList(collect.values());

        }
        Date finalReq = req;
        return allMaKlineResps.stream().sorted(Comparator.comparing(StockKline::getTime).reversed()).filter(stockKline -> stockKline.getTime().compareTo(finalReq.getTime()) <1).limit(klineReq.getNumber()).collect(Collectors.toList());

    }

    @Override
    public List<StockKline> minkList(MinKlineReq minKlineReq) {
        log.info("minkList cache call={}",minKlineReq);
        MinuteKReq  minuteKReq= MinuteKReq.builder()
                .build();
        BeanUtils.copyProperties(minKlineReq,minuteKReq,"date");
        minuteKReq.setDate(DateUtils.formatDate(minKlineReq.getDate()));
        List<AllMaKlineResp> allMaKlineResps = stockKlineCacheService.minkList(minuteKReq);

        if(allMaKlineResps.isEmpty()){
            log.info("minkList cache null={}",minuteKReq);
            stockKlineCacheService.refreshMink(minuteKReq);
            return Lists.newArrayList();
           // allMaKlineResps = stockKlineCacheService.minkList(minuteKReq);
        }

        List<AllMaKlineResp> collect = allMaKlineResps.stream().sorted(Comparator.comparing(AllMaKlineResp::getDate).reversed())
                .filter(allMaKlineResp ->allMaKlineResp.getDate().compareTo(minKlineReq.getDate())<1 ).limit(minKlineReq.getSize()).collect(Collectors.toList());
        log.info("minkList cache end={}",minuteKReq);
        return StockConvert.convertStockKline(collect);

    }

    @Override
    public List<TrendResp> trend(TrendReq trendReq) {
        return klineBusinessApi.trend(trendReq).getData();
    }

    @Override
    public List<TrendResp> trendFive(TrendReq trendReq) {
        return klineBusinessApi.trendFive(trendReq).getData();
    }

}
