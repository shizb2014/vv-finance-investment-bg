package com.vv.finance.investment.bg.api.stock.cache;

import com.fenlibao.security.sdk.ws.core.model.req.KlineReq;
import com.fenlibao.security.sdk.ws.core.model.req.MinuteKReq;
import com.fenlibao.security.sdk.ws.core.model.req.TrendReq;
import com.fenlibao.security.sdk.ws.core.model.resp.TrendResp;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.gateway.dto.req.MinKlineReq;
import com.vv.finance.investment.gateway.dto.resp.AllMaKlineResp;

import java.util.List;

/**
 * 缓存固定条数接口
 * @author hamilton
 * @date 2020/12/10 11:03
 */
public interface StockKlineCacheApi {

    /**
     * 日 k
     * @param klineReq
     * @return
     */
    List<StockKline> dailyList(KlineReq klineReq);

    List<StockKline> minkList(MinKlineReq minKlineReq);

    /**
     * 分时
     * @param trendReq
     * @return
     */
    List<TrendResp> trend(TrendReq trendReq);

    /**
     * 五日分时
     * @param trendReq
     * @return
     */
    List<TrendResp> trendFive(TrendReq trendReq);


}
