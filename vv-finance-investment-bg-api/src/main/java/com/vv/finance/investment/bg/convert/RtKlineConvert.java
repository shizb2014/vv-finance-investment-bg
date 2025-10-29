package com.vv.finance.investment.bg.convert;

import com.fenlibao.security.sdk.ws.core.model.resp.KlineResp;
import com.google.common.base.Converter;
import com.vv.finance.investment.bg.stock.kline.entity.RtStockKline;
import org.springframework.stereotype.Service;

/**
 * @author chenyu
 * @date 2020/11/26 11:21
 */
@Service
public class RtKlineConvert extends Converter<KlineResp, RtStockKline> {
    @Override
    protected RtStockKline doForward(KlineResp klineResp) {
        RtStockKline rtStockKline = new RtStockKline();
        rtStockKline.setChgPct(klineResp.getChg_pct());
        rtStockKline.setChg(klineResp.getChg());
        rtStockKline.setAmount(klineResp.getAmount());
        rtStockKline.setVolume(klineResp.getVolume());
        rtStockKline.setTime(klineResp.getTime().getTime());
        return rtStockKline;
    }

    @Override
    protected KlineResp doBackward(RtStockKline rtStockKline) {
        return null;
    }
}
