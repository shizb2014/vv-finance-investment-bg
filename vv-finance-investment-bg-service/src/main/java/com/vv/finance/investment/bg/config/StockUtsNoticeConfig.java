package com.vv.finance.investment.bg.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author hamilton
 * @date 2020/11/28 16:56
 * stock.uts.notice 在配置中心进行配置
 * financial_report
 * company_change
 * stock_equity
 * trading
 * ipo_file
 * meeting
 * disclosure
 * bonds_and_warrants
 * other
 *
 */
@ConfigurationProperties(prefix = "stock.uts.notice")
@Configuration
@Getter
@Setter
public class StockUtsNoticeConfig {


    private Map<String, List<String>> cacheMap;


}
