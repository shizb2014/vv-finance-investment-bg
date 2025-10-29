package com.vv.finance.investment.bg.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 *
 * @author qinxi
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb.second")
public class SecondMongoConfig extends AbstractMongoConfig {

    @Override
    @Bean(name = "lineShapeTraceMongoTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate((mongoDbFactory()));
    }
}
