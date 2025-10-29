package com.vv.finance.investment;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.stock.StockSceneSimulateApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.info.TradeStatisticsDto;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Set;


/**
 * @Auther: shizb
 * @Date: 2024/7/15
 * @Description: com.vv.finance.investment
 * @version: 1.0
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class StockSceneSimulateTest {

    @Resource
    StockSceneSimulateApi stockSceneSimulateApi;

    @Resource
    RedisClient redisClient;

    @Test
    public void setStockRankingApiTest(){
        System.out.println(stockSceneSimulateApi.findTradingTempSimulateStockByTime(new Date()));
    }

    @Test
    public void StockSceneSimulateTest(){

        //资金分布
        log.info("变更资金分布数据开始：sourceCode：{} targetCode：{}");
        // 1、获取到所有的key
        Set<String> keys = redisClient.getRedisKeys(RedisKeyConstants.TRADE_MAP_CAPITAL_DISTRIBUTION.concat("*"));
        for (String key : keys) {
            TradeStatisticsDto tradeStatisticsDto = redisClient.hget(key, "02516.hk");
            if (ObjectUtils.isNotEmpty(tradeStatisticsDto)) {
                tradeStatisticsDto.setCode("02516-t.hk");
                redisClient.hset(key, "02516-t.hk", tradeStatisticsDto, 0);
            }
        }    }
}
