package com.vv.finance.investment.bg.job.stock;

import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * @Auther: shizb
 * @Date: 2024/5/22
 * @Description: com.vv.finance.investment.bg.job.stock
 * @version: 1.0
 */
@Slf4j
@Component
public class BlockStockJob {

    @Resource
    private StockInfoApi stockInfoApi;

    @Resource
    private HkTradingCalendarApi tradingCalendarApi;

    @XxlJob(value = "updateBlockData", author = "史志彪", cron = "0/5 * * ? * *", desc = "获取港股板块快照数据")
    public ReturnT<String> updateBlockData(String param) {
        log.info("updateBlockData start");
        //盘中执行
        if(StringUtils.isBlank(param)){
            if(!tradingCalendarApi.isTradingDay(LocalDate.now()) || !DateUtils.tradingPeriod()){
                log.info("不更新板块快照");
                return ReturnT.SUCCESS;
            }
        }
        stockInfoApi.getBlockSnapshot();
        log.info("updateBlockData end");
        return ReturnT.SUCCESS;
    }

}
