package com.vv.finance.investment.bg.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author chenyu
 * @date 2021/3/3 14:27
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String datasource = DataSourceContextHolder.getDbType();
        log.debug("使用数据源 {}", datasource);
        return datasource;
    }
}
