package com.vv.finance.investment.bg.job.glue;

import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hamilton
 * @date 2020/11/20 14:13
 * 码表与排行榜同步 -- 在job界面配置GLUE模式执行
 */
public class OmdcJobHandler extends IJobHandler {
    @Autowired
    StockInfoApi stockInfoApi;
    @Autowired
    StockRankingApi stockRankingApi;
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //码表
        stockInfoApi.initStockDefine();
        //排行榜
        stockRankingApi.initIndustrySubsidiary();

        return ReturnT.SUCCESS;
    }
}
