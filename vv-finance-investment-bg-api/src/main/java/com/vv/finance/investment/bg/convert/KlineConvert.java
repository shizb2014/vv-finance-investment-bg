package com.vv.finance.investment.bg.convert;

import com.fenlibao.security.sdk.ws.core.model.resp.IndexKlineResp;
import com.fenlibao.security.sdk.ws.core.model.resp.KlineResp;
import com.google.common.base.Converter;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import org.springframework.stereotype.Service;

/**
 * @author chenyu
 * @date 2020/11/26 11:21
 */
@Service
public class KlineConvert extends Converter<KlineResp, StockKline> {
    @Override
    protected StockKline doForward(KlineResp klineResp) {
        StockKline stockKline = new StockKline();
        stockKline.setClose(klineResp.getClose());
        stockKline.setHigh(klineResp.getHigh());
        stockKline.setLow(klineResp.getLow());
        stockKline.setOpen(klineResp.getOpen());
        stockKline.setPreClose(klineResp.getPreClose());
        stockKline.setAmount(klineResp.getAmount());
        stockKline.setChg(klineResp.getChg());
        stockKline.setChgPct(klineResp.getChg_pct());
        stockKline.setTime(klineResp.getTime().getTime());
        stockKline.setVolume(klineResp.getVolume());

        return stockKline;
    }

    @Override
    protected KlineResp doBackward(StockKline rtStockKline) {
        return null;
    }

    public StockKline convert(IndexKlineResp indexKlineResp) {
        StockKline stockKline = new StockKline();
        stockKline.setClose(indexKlineResp.getClose());
        stockKline.setHigh(indexKlineResp.getHigh());
        stockKline.setLow(indexKlineResp.getLow());
        stockKline.setOpen(indexKlineResp.getOpen());
        stockKline.setPreClose(indexKlineResp.getPreclose());
        stockKline.setAmount(indexKlineResp.getAmount());
        stockKline.setChg(indexKlineResp.getChg());
        stockKline.setChgPct(indexKlineResp.getChg_pct());
        stockKline.setTime(indexKlineResp.getTime());
        return stockKline;
    }
}
