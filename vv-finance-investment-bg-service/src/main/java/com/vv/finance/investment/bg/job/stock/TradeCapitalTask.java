package com.vv.finance.investment.bg.job.stock;


import com.vv.finance.base.dto.ResultT;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @Description
 * @Author liuxing
 * @Create 2023/6/29 13:48
 */
@Slf4j
@Component
public class TradeCapitalTask {

    @Resource
    private RedisClient redisClient;

    @Resource
    private HkTradingCalendarApi tradingCalendarApi;

    @Autowired
    private Executor brokerExecutorService;

    @Resource
    StockApi stockApi;

    @Value("${hold.dde.detail.day:5}")
    private Integer holdDdeDetailDay;

    @XxlJob(value = "delDdeBySizeCriterion", author = "szb", cron = "0 0 3 ? * 2,3,4,5,6 *", desc = "删除过期每分钟的成交额数据")
    public ReturnT<String> delDdeBySizeCriterion(String param) {
        if (!tradingCalendarApi.isTradingDay(LocalDate.now())) {
            log.info("非交易日不删除数据, {}", LocalDateTime.now());
            return ReturnT.SUCCESS;
        }
        log.info("删除过期每分钟的成交额数据, {}", LocalDateTime.now());
        // 获取股票码表 遍历所有的股票
        Set<String> codeList = (Set<String>) redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET);
        if (codeList == null) {
            log.info("获取今日股票失败");
            return ReturnT.SUCCESS;
        }
        ResultT<List<BgTradingCalendar>> lastTradingCalendars = tradingCalendarApi.getLastTradingCalendars(LocalDate.now(), holdDdeDetailDay);
        if (CollectionUtils.isEmpty(lastTradingCalendars.getData())) {
            log.info("获取交易日历失败");
            return ReturnT.FAIL;
        }
        LocalDate endDate = lastTradingCalendars.getData().get(holdDdeDetailDay - 1).getDate();
        long endTime = ZoneDateUtils.getUnixTimeByTodayPoint_yyyy_MM_dd(endDate.toString(), ZoneDateUtils.Asia_HongKong);
        log.info("删除股票数量:{},日期{}",codeList.size(), endDate.toString());
        for(String code : codeList){
            CompletableFuture.runAsync(() -> stockApi.delDdeBySizeCriterion(code, endTime), brokerExecutorService);
        }
        return ReturnT.SUCCESS;
    }




}
