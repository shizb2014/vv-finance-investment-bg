package com.vv.finance.investment.bg.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @描述
 * @创建人 shizhibiao
 * @创建时间 2022/9/27 10:05
 */
@Configuration
public class CaffeineConfig {

    @Bean("brokerNameMapCaffeine")
    public Cache<String, String> brokerNameMapCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build();
    }

    @Bean("IndustryNameMapCaffeine")
    public Cache<String, String> IndustryNameMapCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }

    @Bean("brokerIdMapCaffeine")
    public Cache<String, String> brokerIdMapCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }

}
