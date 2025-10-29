package com.vv.finance.investment.bg.api.impl.stock.cache;

import com.fenlibao.security.sdk.ws.core.model.req.MinkTargetReq;
import com.fenlibao.security.sdk.ws.core.model.req.TargetReq;
import com.fenlibao.security.sdk.ws.core.model.resp.BollResp;
import com.fenlibao.security.sdk.ws.core.model.resp.KdjResp;
import com.fenlibao.security.sdk.ws.core.model.resp.MacdResp;
import com.vv.finance.common.constants.omdc.TargetConstant;
import com.vv.finance.investment.bg.api.stock.cache.StockIndicatorCacheApi;
import com.vv.finance.investment.bg.dto.stock.RsiDto;
import com.vv.finance.investment.bg.stock.indicator.service.StockIndicatorCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/12/10 11:15
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class StockIndicatorCacheApiImpl implements StockIndicatorCacheApi {
    private final StockIndicatorCacheService stockIndicatorCacheService;

    @Override
    public List<BollResp> listBoll(TargetReq targetReq) {
        log.info("listBoll cache targetReq={}",targetReq);
        List<BollResp> bollResps = stockIndicatorCacheService.listBoll(targetReq);
        if(CollectionUtils.isEmpty(bollResps)){
            stockIndicatorCacheService.refresh(TargetConstant.BOLL,targetReq.getType(),targetReq.getCode());
            bollResps = stockIndicatorCacheService.listBoll(targetReq);
        }
        List<BollResp>  respList= bollResps.stream().limit(targetReq.getNumber()).collect(Collectors.toList());
        log.info("listBoll resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<BollResp> listBoll(MinkTargetReq minkTargetReq,Integer number) {
        log.info("listBoll cache minkTargetReq={}",minkTargetReq);
        List<BollResp> bollResps = stockIndicatorCacheService.listBoll(minkTargetReq);
        if(CollectionUtils.isEmpty(bollResps)){
            stockIndicatorCacheService.refreshMink(TargetConstant.BOLL,minkTargetReq.getType(),minkTargetReq.getCode());
            bollResps = stockIndicatorCacheService.listBoll(minkTargetReq);
        }
        List<BollResp>  respList= bollResps.stream().limit(number).collect(Collectors.toList());
        log.info("listBoll resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<KdjResp> listKdj(TargetReq targetReq) {
        log.info("listKdj cache targetReq={}",targetReq);
        List<KdjResp> kdjResps = stockIndicatorCacheService.listKdj(targetReq);
        if(CollectionUtils.isEmpty(kdjResps)){
            stockIndicatorCacheService.refresh(TargetConstant.KDJ,targetReq.getType(),targetReq.getCode());
            kdjResps = stockIndicatorCacheService.listKdj(targetReq);
        }
        List<KdjResp>  respList=  kdjResps.stream().limit(targetReq.getNumber()).collect(Collectors.toList());
        log.info("listKdj resp size={}",respList.size());
        return respList;
    }


    @Override
    public List<KdjResp> listKdj(MinkTargetReq minkTargetReq,Integer number) {
        log.info("listKdj cache minkTargetReq={}",minkTargetReq);
        List<KdjResp> kdjResps = stockIndicatorCacheService.listKdj(minkTargetReq);
        if(CollectionUtils.isEmpty(kdjResps)){
            stockIndicatorCacheService.refreshMink(TargetConstant.KDJ,minkTargetReq.getType(),minkTargetReq.getCode());
            kdjResps = stockIndicatorCacheService.listKdj(minkTargetReq);
        }
        List<KdjResp>  respList=  kdjResps.stream().limit(number).collect(Collectors.toList());
        log.info("listKdj resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<MacdResp> listMacd(TargetReq targetReq) {
        log.info("listMacd cache targetReq={}",targetReq);
        List<MacdResp> macdResps = stockIndicatorCacheService.listMacd(targetReq);
        if(CollectionUtils.isEmpty(macdResps)){
            stockIndicatorCacheService.refresh(TargetConstant.MACD,targetReq.getType(),targetReq.getCode());
            macdResps = stockIndicatorCacheService.listMacd(targetReq);
        }
        List<MacdResp>  respList=  macdResps.stream().limit(targetReq.getNumber()).collect(Collectors.toList());
        log.info("listMacd resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<MacdResp> listMacd(MinkTargetReq minkTargetReq,Integer number) {
        log.info("listMacd cache minkTargetReq={}",minkTargetReq);
        List<MacdResp> macdResps = stockIndicatorCacheService.listMacd(minkTargetReq);
        if(CollectionUtils.isEmpty(macdResps)){
            stockIndicatorCacheService.refreshMink(TargetConstant.MACD,minkTargetReq.getType(),minkTargetReq.getCode());
            macdResps = stockIndicatorCacheService.listMacd(minkTargetReq);
        }
        List<MacdResp>  respList=  macdResps.stream().limit(number).collect(Collectors.toList());
        log.info("listMacd resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<RsiDto> listRsi(TargetReq targetReq) {
        log.info("listRsi cache targetReq={}",targetReq);
        List<RsiDto> rsiDtos = stockIndicatorCacheService.listRsi(targetReq);
        if(CollectionUtils.isEmpty(rsiDtos)){
            stockIndicatorCacheService.refresh(TargetConstant.RSI,targetReq.getType(),targetReq.getCode());
            rsiDtos = stockIndicatorCacheService.listRsi(targetReq);
        }
        List<RsiDto>  respList= rsiDtos.stream().limit(targetReq.getNumber()).collect(Collectors.toList());
        log.info("listRsi resp size={}",respList.size());
        return respList;
    }

    @Override
    public List<RsiDto> listRsi(MinkTargetReq minkTargetReq,Integer number) {
        log.info("listRsi cache minkTargetReq={}",minkTargetReq);
        List<RsiDto> rsiDtos = stockIndicatorCacheService.listRsi(minkTargetReq);
        if(CollectionUtils.isEmpty(rsiDtos)){
            stockIndicatorCacheService.refreshMink(TargetConstant.RSI,minkTargetReq.getType(),minkTargetReq.getCode());
            rsiDtos = stockIndicatorCacheService.listRsi(minkTargetReq);
        }
        List<RsiDto>  respList= rsiDtos.stream().limit(number).collect(Collectors.toList());
        log.info("listRsi resp size={}",respList.size());
        return respList;
    }


}
