package com.vv.finance.investment.bg.cache;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.vv.finance.common.constants.GlobalConstants;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yangpeng
 * @date 2024/10/15 19:22
 * @description
 */
@Slf4j
@Service(value = "stockNameMessageListener")
public class RedisMessageListener implements MessageListener {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StockCache stockCache;
    @Value("${stock.name.topic:stock_name_topic}")
    private String stockTopic;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            MDC.put(GlobalConstants.TRACE_ID, IdUtil.simpleUUID());
            String content = (String) redisTemplate.getValueSerializer().deserialize(message.getBody());
            log.info("接收股票名称消息：{}", content);
            //log.info("commonEventMessageListener={}",msgString);
            String channel = redisTemplate.getStringSerializer().deserialize(message.getChannel());
            if (channel != null && !StringUtils.isEmpty(channel) && content != null) {
                stockCache.updateStockSimpleInfo();
            }
        } catch (Exception e) {
            log.error("stockNameMessageListener occurs error", e);
        }
    }
}

