package com.vv.finance.investment.bg.api.hkRedisHandle;

import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;
import java.util.Map;

/**
 * @Auther: GMC
 * @Date: 2023/11/13 10:40
 * @Description:
 * @version: 1.0
 */
public interface HkRedisHandleApi {

    /**
     * 处理redis map结构
     * @param key
     * @param value
     */
    void handleRedisSetMap(String key, Object value);

    /**
     * HashGet
     * @param key
     * @param item
     */
    <T> T handleRedisGetMap(String key, String item);

    /**
     * 普通缓存获取
     * @param key
     * @return
     * @param <T>
     */
    <T> T handleRedisGet(String key);

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    void handleRedisHmset(String key, Map<String, List<SouthwardCapitalStatistics>> map, long time);

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    void handleRedisSetMap(String key, Object value, long time);

}
