package com.vv.finance.investment.bg.stock.indicator.service;

import com.fenlibao.security.sdk.ws.core.model.req.MinkTargetReq;
import com.fenlibao.security.sdk.ws.core.model.req.TargetReq;
import com.fenlibao.security.sdk.ws.core.model.resp.BollResp;
import com.fenlibao.security.sdk.ws.core.model.resp.KdjResp;
import com.fenlibao.security.sdk.ws.core.model.resp.MacdResp;
import com.fenlibao.security.sdk.ws.core.model.resp.RsiResp;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.dto.stock.RsiDto;
import com.vv.finance.investment.gateway.api.stock.ITargetBusinessApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hamilton
 * @date 2020/12/15 16:05
 */
@Service
@Slf4j
public class StockIndicatorCacheService {
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private ITargetBusinessApi targetBusinessApi;
    @Value("${bg.stock.indicator.cache.total:1}")
    private Integer total;
    @Value("${bg.stock.indicator.cache.req.num:1}")
    private Integer num;
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET,key = "'boll:'+#targetReq.type+':'+#targetReq.code")
    public List<BollResp> listBoll(TargetReq targetReq) {
        log.info("load sdk boll,targetReq={}",targetReq);
        List<BollResp> bollResps= Lists.newArrayList();
        targetReq.setNumber(num);
        targetReq.setDay(new Date());
        boll(targetReq,total,bollResps);
        bollResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return bollResps;
    }
    private void boll(TargetReq targetReq,Integer total,List<BollResp> bollResps){
        ResultT<List<BollResp>> listResultT = targetBusinessApi.listBoll(targetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        bollResps.addAll(listResultT.getData());
        if(bollResps.size()<total){
            int size=total-bollResps.size();
            int number= Math.min(size, 100);
            bollResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            BollResp bollResp = bollResps.get(bollResps.size() - 1);
            targetReq.setDay(DateUtils.nextDate(bollResp.getDate()));
            targetReq.setNumber(number);
            boll(targetReq,total,bollResps);
        }

    }

  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET_MIN,key = "'boll:'+#minkTargetReq.type+':'+#minkTargetReq.code")
    public List<BollResp> listBoll(MinkTargetReq minkTargetReq) {

        minkTargetReq.setDate(DateUtils.formatDate(new Date()));
        List<BollResp> bollResps= Lists.newArrayList();
        mBoll(minkTargetReq,total,bollResps);
        return bollResps;
    }
    private void mBoll(MinkTargetReq minkTargetReq,Integer total,List<BollResp> bollResps){
        ResultT<List<BollResp>> listResultT = targetBusinessApi.listMBoll(minkTargetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        bollResps.addAll(listResultT.getData());
        bollResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        if(bollResps.size()<total){
            BollResp bollResp = bollResps.get(bollResps.size() - 1);
            minkTargetReq.setDate(DateUtils.formatDate(DateUtils.nextTradingDate(bollResp.getDate())));
            mBoll(minkTargetReq,total,bollResps);
        }

    }
  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET,key = "'kdj:'+#targetReq.type+':'+#targetReq.code")
    public List<KdjResp> listKdj(TargetReq targetReq) {
        List<KdjResp> kdjResps =Lists.newArrayList();
        targetReq.setNumber(num);
        targetReq.setDay(new Date());
        kdj(targetReq,total,kdjResps);
        kdjResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        return kdjResps;
    }
    private void kdj(TargetReq targetReq,Integer total,List<KdjResp> kdjResps){
        ResultT<List<KdjResp>> listResultT = targetBusinessApi.listKdj(targetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        kdjResps.addAll(listResultT.getData());
        if(kdjResps.size()<total){
            int size=total-kdjResps.size();
            int number= Math.min(size, 100);
            kdjResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            KdjResp kdjResp = kdjResps.get(kdjResps.size() - 1);
            targetReq.setDay(DateUtils.nextDate(kdjResp.getDate()));
            targetReq.setNumber(number);
            kdj(targetReq,total,kdjResps);
        }

    }

  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET_MIN,key = "'kdj:'+#minkTargetReq.type+':'+#minkTargetReq.code")
    public List<KdjResp> listKdj(MinkTargetReq minkTargetReq) {
        minkTargetReq.setDate(DateUtils.formatDate(new Date()));
        List<KdjResp> kdjResps= Lists.newArrayList();
        mKdj(minkTargetReq,total,kdjResps);
        return kdjResps;
    }
    private void mKdj(MinkTargetReq minkTargetReq,Integer total,List<KdjResp> kdjResps){
        ResultT<List<KdjResp>> listResultT = targetBusinessApi.listMKdj(minkTargetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        kdjResps.addAll(listResultT.getData());
        kdjResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        if(kdjResps.size()<total){
            KdjResp kdjResp = kdjResps.get(kdjResps.size() - 1);
            minkTargetReq.setDate(DateUtils.formatDate(DateUtils.nextTradingDate(kdjResp.getDate())));
            mKdj(minkTargetReq,total,kdjResps);
        }

    }


  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET,key = "'macd:'+#targetReq.type+':'+#targetReq.code",sync = true)
    public List<MacdResp> listMacd(TargetReq targetReq) {
        List<MacdResp> macdResps =Lists.newArrayList();
        targetReq.setNumber(num);
        targetReq.setDay(new Date());
        macd(targetReq,total,macdResps);
        macdResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        return macdResps;
    }

  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET_MIN,key = "'macd:'+#minkTargetReq.type+':'+#minkTargetReq.code",sync = true)
    public List<MacdResp> listMacd(MinkTargetReq minkTargetReq) {
        minkTargetReq.setDate(DateUtils.formatDate(new Date()));
        List<MacdResp> macdResps= Lists.newArrayList();
        mMacd(minkTargetReq,total,macdResps);

        return macdResps;
    }
    private void mMacd(MinkTargetReq minkTargetReq,Integer total,List<MacdResp> macdResps){
        ResultT<List<MacdResp>> listResultT = targetBusinessApi.listMMacd(minkTargetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        macdResps.addAll(listResultT.getData());
        macdResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        if(macdResps.size()<total){
            MacdResp macdResp = macdResps.get(macdResps.size() - 1);
            minkTargetReq.setDate(DateUtils.formatDate(DateUtils.nextTradingDate(macdResp.getDate())));
            mMacd(minkTargetReq,total,macdResps);
        }

    }
    private void macd(TargetReq targetReq,Integer total,List<MacdResp> macdResps){
        ResultT<List<MacdResp>> listResultT = targetBusinessApi.listMacd(targetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        macdResps.addAll(listResultT.getData());
        if(macdResps.size()<total){
            int size=total-macdResps.size();
            int number= Math.min(size, 100);
            macdResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            MacdResp macdResp = macdResps.get(macdResps.size() - 1);
            targetReq.setDay(DateUtils.nextDate(macdResp.getDate()));
            targetReq.setNumber(number);
            macd(targetReq,total,macdResps);
        }

    }
  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET,key = "'rsi:'+#targetReq.type+':'+#targetReq.code",sync = true)
    public List<RsiDto> listRsi(TargetReq targetReq) {
        targetReq.setDay(new Date());
        targetReq.setNumber(num);
        TargetReq rsi6=new TargetReq();
        TargetReq rsi12=new TargetReq();
        BeanUtils.copyProperties(targetReq,rsi6);
        BeanUtils.copyProperties(targetReq,rsi12);
        rsi6.setPeriod(6);
        rsi12.setPeriod(12);
        List<RsiResp> rsi6Resps=Lists.newArrayList();
        List<RsiResp> rsi12Resps=Lists.newArrayList();
        rsi(rsi6,total,rsi6Resps);
        rsi(rsi12,total,rsi12Resps);
        return mergeRsi(rsi6Resps,rsi12Resps);
    }
    private List<RsiDto> mergeRsi(List<RsiResp> rsi6Resps,List<RsiResp> rsi12Resps){

        Map<String,RsiDto> rsiDtoMap= Maps.newHashMap();
        rsi6Resps.forEach(rsiResp -> {
            String date = DateUtils.formatDate(rsiResp.getDate(), "yyyy-MM-dd HH:mm:ss");
            RsiDto rsiDto = new RsiDto();
            rsiDto.setRsi6(rsiResp);
            rsiDto.setDate(rsiResp.getDate());
            rsiDtoMap.put(date,rsiDto);
        });


        rsi12Resps.forEach(rsiResp -> {
            String date=DateUtils.formatDate(rsiResp.getDate(),"yyyy-MM-dd HH:mm:ss");
            RsiDto rsiDto = rsiDtoMap.get(date);
            if(rsiDto ==null){
                rsiDto=new RsiDto();
                rsiDto.setDate(rsiResp.getDate());
                rsiDtoMap.put(date,rsiDto);
            }
            rsiDto.setRsi12(rsiResp);
        });
        List<RsiDto> rsiDtos = Lists.newArrayList(rsiDtoMap.values());
        rsiDtos.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        return rsiDtos ;
    }
    private void rsi(TargetReq targetReq,Integer total,List<RsiResp> rsiResps){
        ResultT<List<RsiResp>> listResultT = targetBusinessApi.listRsi(targetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        rsiResps.addAll(listResultT.getData());
        if(rsiResps.size()<total){
            int size=total-rsiResps.size();
            int number= Math.min(size, 100);
            rsiResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            RsiResp rsiResp = rsiResps.get(rsiResps.size() - 1);
            targetReq.setDay(DateUtils.nextDate(rsiResp.getDate()));
            targetReq.setNumber(number);
            rsi(targetReq,total,rsiResps);
        }

    }
  
    @Cacheable(value = RedisKeyConstants.BG_CACHE_TARGET_MIN,key = "'rsi:'+#minkTargetReq.type+':'+#minkTargetReq.code",sync = true)
    public List<RsiDto> listRsi(MinkTargetReq minkTargetReq) {
        minkTargetReq.setDate(DateUtils.formatDate(new Date()));
        MinkTargetReq rsi6=new MinkTargetReq();
        MinkTargetReq rsi12=new MinkTargetReq();
        BeanUtils.copyProperties(minkTargetReq,rsi6);
        BeanUtils.copyProperties(minkTargetReq,rsi12);
        rsi6.setPeriod(6);
        rsi12.setPeriod(12);
        List<RsiResp> rsi6Resps=Lists.newArrayList();
        List<RsiResp> rsi12Resps=Lists.newArrayList();
        mRsi(rsi6,total,rsi6Resps);
        mRsi(rsi12,total,rsi12Resps);
        return mergeRsi(rsi6Resps,rsi12Resps);
    }
    private void mRsi(MinkTargetReq minkTargetReq,Integer total,List<RsiResp> rsiResps){
        ResultT<List<RsiResp>> listResultT = targetBusinessApi.listMRsi(minkTargetReq);
        if(CollectionUtils.isEmpty(listResultT.getData())){
            return;
        }
        rsiResps.addAll(listResultT.getData());
        if(rsiResps.size()<total){
            rsiResps.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            RsiResp rsiResp = rsiResps.get(rsiResps.size() - 1);
            minkTargetReq.setDate(DateUtils.formatDate(DateUtils.nextTradingDate(rsiResp.getDate())));
            mRsi(minkTargetReq,total,rsiResps);
        }

    }
  
    @CacheEvict(value = RedisKeyConstants.BG_CACHE_TARGET,key = "#target+':'+#type+':'+#code")
    public void refresh(String target,String type,String code) {
    }

  
    @CacheEvict(value = RedisKeyConstants.BG_CACHE_TARGET_MIN,key = "#target+':'+#type+':'+#code")
    public void refreshMink(String target,Integer type,String code) {

    }
}
