package com.vv.finance.investment.bg.job.glue;

import com.fenlibao.security.sdk.ws.core.model.req.KlineReq;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.stock.kline.service.StockKlineCacheService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @author hamilton
 * @date 2020/11/20 14:13
 */
public class CacheJobHandler extends IJobHandler {
    @Autowired
    StockKlineCacheService stockKlineCacheService;
    @Autowired
    StockInfoApi stockInfoApi;
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        String yyyyMMdd = DateUtils.formatDate(new Date(), "yyyyMMdd");
        List<String> data = stockInfoApi.allStockDefineCodes().getData();
        long day=Long.parseLong(yyyyMMdd);
        for (String code : data) {
            stockKlineCacheService.dailyList(KlineReq.builder()
                    .mode("rt").adjhkt("")
                    .day(day).code(code)
                    .number(10)
                    .build());
        }

        return ReturnT.SUCCESS;
    }
}
