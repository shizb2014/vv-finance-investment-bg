package com.vv.finance.investment.bg.job.stock;

import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
//import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import com.vv.finance.investment.gateway.dto.resp.HKNewStockResp;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

//import javax.annotation.Resource;
//import java.time.LocalDate;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author liuxing
 * @Create 2023/6/29 13:48
 */
@Slf4j
@Component
public class NewStockTask {

    @Resource
    private StockInfoApi stockInfoApi;

    @Autowired
    private RedisClient redisClient;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private IStockBusinessApi stockBusinessApi;

//    @Resource
//    private HkTradingCalendarApi tradingCalendarApi;

    /**
     * 将全部新股数据缓存到redis，按上市时间倒序排列
     *
     * @param param
     * @return
     */
    @XxlJob(value = "saveAllNewStocksToRedis", author = "刘兴", desc = "将港股全部新股数据缓存到redis", cron = "0/5 * 9-17 ? * *")
    public ReturnT<String> saveAllNewStocksToRedis(String param) {
        log.info("缓存全部新股数据启动, {}", LocalDateTime.now());
//        if(!tradingCalendarApi.isTradingDay(LocalDate.now())){
//            log.warn("缓存全部新股数据不处理，今天是非交易日");
//        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("缓存全部新股数据");
        ResultT<List<HKNewStockResp>> resultT = stockBusinessApi.listIpo();
        if(ResultCode.SUCCESS.code()==resultT.getCode()){
            List<HKNewStockResp> list = resultT.getData();
            log.info("缓存全部新股数据，共获取到{}条记录", list.size());
            List<String> stockCodeList = list.stream().map(s -> s.getSymbol()).collect(Collectors.toList());
            Map<String, Long> stockCodeIdMap = stockInfoApi.selectStockIdByCodes(stockCodeList);
            list.stream().forEach(t -> t.setStockId(stockCodeIdMap.get(t.getSymbol())));
            redisClient.set(RedisKeyConstants.BG_HK_NEW_STOCK_LIST,list);
        }
        stopWatch.stop();
        stopWatch.prettyPrint();
        log.info("缓存全部新股数据结束, 耗时{}s", stopWatch.getTotalTimeSeconds());
        return ReturnT.SUCCESS;
    }
}
