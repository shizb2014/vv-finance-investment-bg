package com.vv.finance.investment.bg.api.stock.cache;

import com.fenlibao.security.sdk.ws.core.model.req.MinkTargetReq;
import com.fenlibao.security.sdk.ws.core.model.req.TargetReq;
import com.fenlibao.security.sdk.ws.core.model.resp.BollResp;
import com.fenlibao.security.sdk.ws.core.model.resp.KdjResp;
import com.fenlibao.security.sdk.ws.core.model.resp.MacdResp;
import com.vv.finance.investment.bg.dto.stock.RsiDto;

import java.util.List;

/**
 * @author hamilton
 * @date 2020/12/10 10:18
 */
public interface StockIndicatorCacheApi {

    List<BollResp> listBoll(TargetReq targetReq);
    List<BollResp> listBoll(MinkTargetReq minkTargetReq,Integer number);

    List<KdjResp> listKdj(TargetReq targetReq);
    List<KdjResp> listKdj(MinkTargetReq minkTargetReq,Integer number);

    List<MacdResp> listMacd(TargetReq targetReq);
    List<MacdResp> listMacd(MinkTargetReq minkTargetReq,Integer number);

    /**
     * 获取12 和6
     * @param targetReq period 不需要
     * @return
     */
    List<RsiDto> listRsi(TargetReq targetReq);
    List<RsiDto> listRsi(MinkTargetReq minkTargetReq,Integer number);


}
