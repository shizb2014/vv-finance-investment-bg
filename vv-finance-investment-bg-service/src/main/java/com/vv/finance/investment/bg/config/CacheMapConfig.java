package com.vv.finance.investment.bg.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author hamilton
 * @date 2020/11/28 16:56
 */
@ConfigurationProperties(prefix = "vv.redis.cache.value")
@Configuration
@Getter
@Setter
public class CacheMapConfig {


    private Map<String,Integer> cacheMap;


}
