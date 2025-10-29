
package com.vv.finance.investment.bg.api.impl.hkRedisHandle;

import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.api.hkRedisHandle.HkRedisHandleApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.poi.ss.formula.functions.T;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Auther: shizb
 * @Date: 2024/8/21
 * @Description: com.vv.finance.investment.us.bg.api.impl.RedisHandle
 * @version: 1.0
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class HkRedisHandleImpl implements HkRedisHandleApi {

    @Resource
    RedisClient redisClient;

    @Override
    public void handleRedisSetMap(String key, Object value){
        redisClient.set(key, value);
    }

    @Override
    public void handleRedisSetMap(String key, Object value, long time){
        redisClient.set(key, value, time);
    }

    @Override
    public <T> T handleRedisGetMap(String key, String item){
        return redisClient.hget(key, item);
    }

    @Override
    public <T> T handleRedisGet(String key){
        return redisClient.get(key);
    }

    @Override
    public void handleRedisHmset(String key, Map<String, List<SouthwardCapitalStatistics>> map, long time){
        redisClient.hmset(key, map, time);
    }

}
