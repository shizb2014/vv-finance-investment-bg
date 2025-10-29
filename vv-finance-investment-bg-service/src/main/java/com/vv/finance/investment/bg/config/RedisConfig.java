package com.vv.finance.investment.bg.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Maps;
import com.vv.finance.base.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableCaching
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    private LettuceConnectionFactory redisConnectionFactory;
    @Autowired
    CacheMapConfig cacheMapConfig;
    @Value("${stock.name.topic:stock_name_topic}")
    private String stockTopic;

    @Bean
    public RedisTemplate<String, Object> redisCacheTemplate() {
        return RedisUtils.buildRedisTemplate(redisConnectionFactory);
    }


    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,

                JsonTypeInfo.As.WRAPPER_ARRAY);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        om.registerModule(javaTimeModule);
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(om);
        return genericJackson2JsonRedisSerializer;

    }

    @Bean(destroyMethod = "destroy")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("stockNameMessageListener") MessageListener stockNameMessageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        //可以添加多个 messageListener
        container.addMessageListener(stockNameMessageListener, new PatternTopic(stockTopic));
        return container;
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(o.getClass().getName()).append(".");
            sb.append(method.getName()).append(".");
            if (objects.length > 0) {
                for (Object obj : objects) {
                    sb.append(obj);

                }
            }
            return sb.toString();
        };
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(Objects.requireNonNull(cacheManager()));
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("cacheGetError cache={},key={}", cache, key, exception);

            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
                log.error("cachePutError cache={},key={},value={}", cache, key, value, exception);

            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("cacheEvictError cache={},key={}", cache, key, exception);


            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("cacheEvictError cache={}", cache, exception);

            }
        };
    }

    @Bean
    public RedisLockRegistry redisLockRegistry() {
        return new RedisLockRegistry(redisConnectionFactory, "REDIS_LOCK");
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        Map<String, RedisCacheConfiguration> configurationMap = Maps.newConcurrentMap();
        for (Map.Entry<String, Integer> cacheEntry : cacheMapConfig.getCacheMap().entrySet()) {
            configurationMap.put(cacheEntry.getKey(), this.redisCacheConfiguration(cacheEntry.getValue()));
        }

        return RedisCacheManager.builder(redisConnectionFactory).
                cacheDefaults(this.redisCacheConfiguration(-1))
                .withInitialCacheConfigurations(configurationMap).build();

    }

    private RedisCacheConfiguration redisCacheConfiguration(Integer seconds) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(seconds))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
    }

//    /**
//     * 自定义LettuceConnectionFactory
//     */
//    private LettuceConnectionFactory createLettuceConnectionFactory() {
//        //redis配置
//        RedisConfiguration redisConfiguration = new RedisClusterConfiguration(Arrays.asList(hostName.split(",")));
//        ((RedisClusterConfiguration) redisConfiguration).setPassword(password);
//        //连接池配置
//        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
//        genericObjectPoolConfig.setMaxIdle(maxIdle);
//        genericObjectPoolConfig.setMinIdle(minIdle);
//        genericObjectPoolConfig.setMaxTotal(maxActive);
//        genericObjectPoolConfig.setMaxWaitMillis(maxWait);
//        //redis客户端配置
//        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
//                builder = LettucePoolingClientConfiguration.builder().
//                commandTimeout(Duration.ofMillis(timeOut));
//
////        builder.shutdownTimeout(Duration.ofMillis(shutdownTimeOut));
//        builder.poolConfig(genericObjectPoolConfig);
//        LettuceClientConfiguration lettuceClientConfiguration = builder.build();
//        //根据配置和客户端配置创建连接
//        LettuceConnectionFactory lettuceConnectionFactory = new
//                LettuceConnectionFactory(redisConfiguration, lettuceClientConfiguration);
//        lettuceConnectionFactory.afterPropertiesSet();
//        return lettuceConnectionFactory;
//
//    }

}














/*
 * package com.vv.finance.investment.config;
 *
 *
 *
 *
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.cache.annotation.CachingConfigurerSupport; import
 * org.springframework.cache.annotation.EnableCaching; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.data.redis.connection.RedisConnectionFactory; import
 * org.springframework.data.redis.core.RedisTemplate; import
 * org.springframework.data.redis.serializer.StringRedisSerializer; import
 * com.fasterxml.jackson.annotation.JsonAutoDetect; import
 * com.fasterxml.jackson.annotation.PropertyAccessor; import
 * com.fasterxml.jackson.databind.ObjectMapper;
 *
 *//**
 * redis配置
 *
 * @author Danny
 *//*
 * @Configuration
 *
 * public class RedisConfig extends CachingConfigurerSupport {
 *
 * @Autowired private RedisConnectionFactory connectionFactory;
 *
 * @Bean public RedisTemplate<String, Object> redisTemplate() {
 * RedisTemplate<String, Object> template = new RedisTemplate<>();
 * template.setConnectionFactory(connectionFactory);
 *
 * FastJson2JsonRedisSerializer serializer = new
 * FastJson2JsonRedisSerializer(Object.class);
 *
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
 * mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
 * serializer.setObjectMapper(mapper);
 *
 * template.setValueSerializer(serializer); //
 * 使用StringRedisSerializer来序列化和反序列化redis的key值 template.setKeySerializer(new
 * StringRedisSerializer()); template.afterPropertiesSet(); return template; } }
 */