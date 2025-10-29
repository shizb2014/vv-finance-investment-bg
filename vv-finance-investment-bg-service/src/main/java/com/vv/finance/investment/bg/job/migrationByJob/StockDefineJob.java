package com.vv.finance.investment.bg.job.migrationByJob;

import cn.hutool.core.text.StrPool;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.collect.Sets;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.receiver.Stockdefine;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName StockDefineJob
 * @Deacription 股票码表定时job
 * @Author lh.sz
 * @Date 2021年04月07日 16:29
 **/
@Component
@Slf4j
public class StockDefineJob {

    @Autowired
    private RedisClient redisBasisClient;
    private static final String PARSE = "yyyyMMdd";
    @Resource
    HkTradingCalendarApi tradingCalendarApi;



    /**
     * 缓存最新的码表
     *
     * @return ReturnT
     */
    @XxlJob(value = "setNewestStockDefine", author = "罗浩", cron = "55 59 8-10 ? * 2-6 *", desc = "缓存每日最新的码表")
    public ReturnT<String> setNewestStockDefine(String param) {
        if (tradingCalendarApi.isTradingDay(DateUtils.date2LocalDate(new Date()))) {
            Set<String> codes = redisBasisClient.sGet(RedisKeyConstants.RECEIVER_DAILY_STOCK_DEFINE_CODE.concat(DateUtils.formatDate(new Date(), PARSE)));
            List<String> stopCodes = getStopCode();
            Set<String> stockNameAndCode = codes.stream()
                    .filter(s -> !stopCodes.contains(s))
                    .map(s -> {
                        Stockdefine stockdefine = redisBasisClient.get(RedisKeyConstants.RECEIVER_STOCK_DEFINE_BEAN.concat(s));
                        return s.concat(StrPool.COMMA).concat(stockdefine.getName());
                    }).collect(Collectors.toSet());
            // 过滤停牌股票
            Set<String> newCodes = Sets.newHashSet();
            codes.forEach(code->{
                if(!stopCodes.contains(code)){
                    newCodes.add(code);
                }
            });
            redisBasisClient.del(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET);
            redisBasisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET, stockNameAndCode);
            redisBasisClient.del(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET);
            redisBasisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET, codes);
        }
        return ReturnT.SUCCESS;
    }


    private List<String> getStopCode() {
        Set<String> stopCodeSet = redisBasisClient.getKeys(RedisKeyConstants.RECEIVER_SECURITY_STATUS_BEAN.concat("*"));
        log.info("============停牌股票===========stopCodeSet:{}", stopCodeSet);
        List<String> stopCodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stopCodeSet)) {
            stopCodeSet.forEach(code -> {
                String[] split = code.split(":");
                stopCodes.add(split[3]);
            });

        }
        return stopCodes;
    }

}
