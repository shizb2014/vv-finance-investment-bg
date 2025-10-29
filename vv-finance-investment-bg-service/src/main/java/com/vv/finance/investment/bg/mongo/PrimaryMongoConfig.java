package com.vv.finance.investment.bg.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 *
 * 默认为此库
 * @author qinxi
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb.primary")
public class PrimaryMongoConfig extends AbstractMongoConfig {

    @Override
    @Primary
    @Bean(name = "mongoTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate((mongoDbFactory()));
    }
}
