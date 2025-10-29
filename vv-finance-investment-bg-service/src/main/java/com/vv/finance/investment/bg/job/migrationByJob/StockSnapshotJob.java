package com.vv.finance.investment.bg.job.migrationByJob;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.Adjhkt;
import com.vv.finance.common.entity.common.StockMoveTheme;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockMoveApi;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.move.StockMove;
import com.vv.finance.investment.bg.entity.uts.Xnhks0503;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName StockSnapshotJob
 * @Deacription 处理快照的job
 * @Author GMC
 * @Date 2024年07月26日 15:45
 **/
@Component
@Slf4j
public class StockSnapshotJob {
    @Resource
    IStockMarketService stockMarketService;
    @Resource
    HkTradingCalendarApi tradingCalendarApi;
    @Resource
    UtsInfoService utsInfoService;

    @Resource
    StockMoveApi stockMoveApi;
    @Autowired
    private  RedisClient redisBasisClient;

    /**
     * 异动数据落库
     *
     * @return
     */
    @XxlJob(value = "saveMoveStockByRedis", author = "吴世亮", cron = "0 10 16 ? * 2-6 *", desc = "异动数据落库")
    public ReturnT<String> saveMoveStockByRedis(String param) {
        if (!tradingCalendarApi.isTradingDay(LocalDate.now())) {
            log.info("当前非交易日");
            return ReturnT.SUCCESS;
        }
        // 删除上一个交易日的缓存
        BgTradingCalendar beforeTradingCalendar = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now());
        redisBasisClient.del(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + beforeTradingCalendar.getDate());
        String today = DateUtils.today();
        List<Object> objects = redisBasisClient.lGet(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + today, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            log.info("异动数据查询为空！");
            return ReturnT.SUCCESS;
        }
        List<String> stockCodes = new ArrayList<>(objects.size());
        List<StockMove> collect = objects.stream().map(object -> {
            StockMove stockMove = new StockMove();
            StockMoveTheme stockMoveTheme = (StockMoveTheme) object;
            stockMove.setCode(stockMoveTheme.getCode());
            stockMove.setName(stockMoveTheme.getName());
            stockMove.setMoveType(stockMoveTheme.getMoveType());
            stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
            stockMove.setTime(stockMoveTheme.getTime());
            stockMove.setCreateTime(new Date());
            stockMove.setUpdateTime(new Date());
            stockMove.setMoveNum(stockMoveTheme.getMoveNum());
            stockMove.setSerialId(stockMoveTheme.getSerialId());
            stockCodes.add(stockMoveTheme.getCode());
            return stockMove;
        }).collect(Collectors.toList());
        Boolean aBoolean = stockMoveApi.saveBatch(collect);
//        if (aBoolean) {
        log.info("异动数据落库成功！股票代码{}", stockCodes);
//        } else {
//            log.info("异动数据落库失败！股票代码{}", stockCodes);
//        }
        return ReturnT.SUCCESS;
    }

    /**
     * 获取招股价
     *
     * @return
     */
    @XxlJob(value = "cacheNewStockProClose", author = "罗浩", cron = "0 05 5,6,9 ? * 2-6 *", desc = "缓存新股的收市价")
    public ReturnT<String> cacheNewStockProClose(String param) {
        if (tradingCalendarApi.isTradingDay(LocalDate.now())) {
            redisBasisClient.del(RedisKeyConstants.NEW_STOCK_DETAIL_MAP);
            Set<String> newStockSet = new HashSet<>();
            Map<String, String> codeAndProCloseMap = new HashMap<>(30);
            long nowDate = Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));
            List<Object> xnhks0501List = stockMarketService.getXnhks0101(nowDate).getData();
            if (CollectionUtils.isNotEmpty(xnhks0501List)) {
                newStockSet = xnhks0501List.stream().map(Objects::toString).collect(Collectors.toSet());
            }
            if (CollectionUtils.isNotEmpty(newStockSet)) {
                //获取每日的新股
                List<Xnhks0503> list = utsInfoService.getXnhks0503ByCodes(newStockSet, System.currentTimeMillis());
                if (CollectionUtils.isNotEmpty(list)) {
                    list.forEach(x -> {
                        if (x.getF011n() == null) {
                            codeAndProCloseMap.put(x.getSeccode(), "");
                        } else {
                            codeAndProCloseMap.put(x.getSeccode(), x.getF011n().toString());
                        }
                    });
                }
                //放入每日新股的招股价
                redisBasisClient.set(RedisKeyConstants.NEW_STOCK_DETAIL_MAP, codeAndProCloseMap);
            }
        }
        return ReturnT.SUCCESS;
    }
}
