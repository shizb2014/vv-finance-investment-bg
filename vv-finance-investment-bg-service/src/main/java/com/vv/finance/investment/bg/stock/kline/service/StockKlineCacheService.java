package com.vv.finance.investment.bg.stock.kline.service;

import cn.hutool.core.date.DateUtil;
import com.fenlibao.security.sdk.ws.core.model.req.IndexReq;
import com.fenlibao.security.sdk.ws.core.model.req.KlineReq;
import com.fenlibao.security.sdk.ws.core.model.req.MinuteKReq;
import com.fenlibao.security.sdk.ws.core.model.resp.IndexKlineResp;
import com.fenlibao.security.sdk.ws.core.model.resp.KlineResp;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.OmdcMode;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.gateway.api.index.IndexKlineServiceApi;
import com.vv.finance.investment.gateway.api.stock.IKlineBusinessApi;
import com.vv.finance.investment.gateway.dto.resp.AllMaKlineResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/12/15 15:57
 */
@Component
@Slf4j
public class StockKlineCacheService {
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice",timeout = 16000)
    IKlineBusinessApi klineBusinessApi;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice",timeout = 16000)
    IndexKlineServiceApi indexKlineServiceApi;

//    @DubboReference(group = "${dubbo.investment.composite.service.group:composite}", registry = "compositeservice")
//    private HkStockCompositeApi compositeApi;

    @Value("${bg.stock.kline.cache.sdk.total:1}")
    private Integer sdkTotal;
    @Value("${bg.stock.kline.cache.req.num:1}")
    private Integer num;
    @Value("${bg.stock.kline.cache.total:1}")
    private Integer total;

   
    @Cacheable(value = RedisKeyConstants.BG_CACHE_KLINE,key = "'day:'+#klineReq.code+'-'+#klineReq.adjhkt")
    public List<StockKline> dailyList(KlineReq klineReq) {
        List<AllMaKlineResp> stockKlines= Lists.newArrayList();
        klineReq.setMode(OmdcMode.RT);
        klineReq.setNumber(num);
        listAllMaDaily(stockKlines,klineReq,sdkTotal);
        stockKlines.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        return stockKlines.stream().map(allMaKlineResp -> {
            StockKline stockKline=new StockKline();
            BeanUtils.copyProperties(allMaKlineResp,stockKline);
            return stockKline;
        }).collect(Collectors.toList());
    }
    @Cacheable(value = RedisKeyConstants.BG_FIXED_CACHE_KLINE,key = "'daily:'+#klineReq.code")
    public List<StockKline> queryDailyKlineFixedCache(KlineReq klineReq){
//        List<KlineEntity> klineEntities = compositeApi.selectKlineList(StockKlineReq.builder()
//                .adjhkt(klineReq.getAdjhkt())
//                .code(klineReq.getCode())
//                .num(300).time(System.currentTimeMillis())
//                .type("day")
//                .build());
//        return klineEntities.stream().map(klineEntity -> {
//            StockKline stockKline=new StockKline();
//            BeanUtils.copyProperties(klineEntity,stockKline);
//            return stockKline;
//        }).collect(Collectors.toList());
        return null;
    }
    @CacheEvict(value = RedisKeyConstants.BG_FIXED_CACHE_KLINE,key = "'daily:'+#klineReq.code")
    public void clearDailyKlineFixedCache(KlineReq klineReq) {

    }
    private void listAllMaDaily(List<AllMaKlineResp> stockKlines,KlineReq klineReq,Integer total){
        List<KlineResp> klineResps;
        if(klineReq.getCode().contains(".hk")) {
            klineResps= klineBusinessApi.listDaily(klineReq).getData();
        }else {
            ResultT<List<IndexKlineResp>> listResultT = indexKlineServiceApi.listDailyK(IndexReq.builder().number(klineReq.getNumber()
            ).mode(klineReq.getMode()).day(klineReq.getDay()).indexcode(klineReq.getCode()).build());
            klineResps=listResultT.getData().stream().map(indexKlineResp -> {
                KlineResp klineResp=new KlineResp();
                klineResp.setPreClose(indexKlineResp.getPreclose());
                try {
                    klineResp.setTime(DateUtils.parseDate(indexKlineResp.getTime().toString(),"yyyy-MM-dd"));
                } catch (ParseException e) {
                   log.error("listAllMaDaily",e);
                }
                BeanUtils.copyProperties(indexKlineResp,klineResp);
                return klineResp;
            }).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(klineResps)){
            return;
        }
        List<AllMaKlineResp> data = klineResps.stream().map(klineResp -> {
            AllMaKlineResp allMaKlineResp = new AllMaKlineResp();
            BeanUtils.copyProperties(klineResp, allMaKlineResp);
            allMaKlineResp.setDate(klineResp.getTime());
            allMaKlineResp.setTime(klineResp.getTime().getTime());
            allMaKlineResp.setCode(klineReq.getCode());
            allMaKlineResp.setMode(klineReq.getMode());
            allMaKlineResp.setChgPct(klineResp.getChg_pct());
            allMaKlineResp.setAdjhkt(klineReq.getAdjhkt());
            return allMaKlineResp;
        }).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        List<AllMaKlineResp> collect = data.stream().map(allMaKlineResp -> {
            AllMaKlineResp stockKline = new AllMaKlineResp();
            BeanUtils.copyProperties(allMaKlineResp, stockKline);
            return stockKline;
        }).collect(Collectors.toList());
        stockKlines.addAll(collect);

        if(stockKlines.size()<total) {
            int size=total-stockKlines.size();
            int num= Math.min(size, 100);
            collect.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            AllMaKlineResp stockKline = collect.get(collect.size() - 1);
            klineReq.setNumber(num);
            klineReq.setDay(Long.parseLong(DateUtil.format(DateUtils.nextDate(stockKline.getTime()),"yyyyMMdd")));
            listAllMaDaily(stockKlines,klineReq,sdkTotal);
        }

    }
   
    @Cacheable(value = RedisKeyConstants.BG_CACHE_KLINE_MIN,key = "#minuteKReq.minc+':'+#minuteKReq.code+'-'+#minuteKReq.adjhkt")
    public List<AllMaKlineResp> minkList(MinuteKReq minuteKReq) {
        log.info("minkList real call={}",minuteKReq);
        List<AllMaKlineResp> stockKlines= Lists.newArrayList();
        minuteKReq.setMode(OmdcMode.RT);
        stockMinList(minuteKReq,stockKlines,sdkTotal);
        stockKlines.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        log.info("minkList real call end={}",minuteKReq);
        return stockKlines;
    }
    private void stockMinList(MinuteKReq minuteKReq, List<AllMaKlineResp> stockKlines,Integer total){
        List<KlineResp> klineResps;
        if(minuteKReq.getCode().contains(".hk")) {
            klineResps= klineBusinessApi.listMinuteK(minuteKReq).getData();
        }else {
            klineResps = indexKlineServiceApi.listMinuteK(minuteKReq).getData();
        }
        if(CollectionUtils.isEmpty(klineResps)){
            return;
        }
        List<AllMaKlineResp> data = klineResps.stream().map(klineResp -> {
            AllMaKlineResp allMaKlineResp = new AllMaKlineResp();
            BeanUtils.copyProperties(klineResp, allMaKlineResp);
            allMaKlineResp.setDate(klineResp.getTime());
            allMaKlineResp.setTime(klineResp.getTime().getTime());
            allMaKlineResp.setCode(minuteKReq.getCode());
            allMaKlineResp.setMode(minuteKReq.getMode());
            allMaKlineResp.setChgPct(klineResp.getChg_pct());
            allMaKlineResp.setAdjhkt(minuteKReq.getAdjhkt());
            return allMaKlineResp;
        }).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        List<AllMaKlineResp> stockKlineList=data.stream().map(allMaKlineResp -> {
            AllMaKlineResp stockKline=new AllMaKlineResp();
            BeanUtils.copyProperties(allMaKlineResp,stockKline);
            return stockKline;
        }).sorted((o1, o2) -> o2.getTime().compareTo(o1.getTime())).collect(Collectors.toList());
        stockKlines.addAll(stockKlineList);
        if(stockKlines.size()<total) {
            AllMaKlineResp stockKline= stockKlineList.get(stockKlineList.size()-1);
            Date date=nextMin(stockKline.getTime());
            minuteKReq.setDate(DateUtils.formatDate(date));
            stockMinList(minuteKReq,stockKlines,total);
        }
    }

    private Date nextMin(long time){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH,day-1);
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        if(i==1||i==7){
            return nextMin(calendar.getTime().getTime());
        }
        return calendar.getTime();
    }
   
    @CacheEvict(value =RedisKeyConstants.BG_CACHE_KLINE,key = "'day:'+#klineReq.code+'-'+#klineReq.adjhkt")
    public void refreshDaily(KlineReq klineReq) {
    }

   
    @CacheEvict(value = RedisKeyConstants.BG_CACHE_KLINE_MIN,key = "#minuteKReq.minc+':'+#minuteKReq.code+'-'+#minuteKReq.adjhkt")
    public void refreshMink(MinuteKReq minuteKReq) {
    }

}
