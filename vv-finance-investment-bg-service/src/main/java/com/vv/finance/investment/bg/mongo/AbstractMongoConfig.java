package com.vv.finance.investment.bg.mongo;

import com.mongodb.ConnectionString;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB多数据源抽象类
 * 目前存在数据库：
 * @author qinxi
 */
public abstract class AbstractMongoConfig {

    private String uri;

    /**
     * 获取mongoDBTemplate对象
     */
    public abstract MongoTemplate getMongoTemplate() throws Exception;

    /**
     * 创建mongoDb工厂
     */
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(new ConnectionString(uri));
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
