package com.vv.finance.investment.bg;


import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.vv.finance.base.health.HealthController;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {"com.vv.finance.common","com.vv.finance.investment.bg","com.vv.minio.starter"}, exclude = {DataSourceAutoConfiguration.class})
@EnableDubbo
@MapperScan({"com.vv.finance.investment.bg.**.mapper"})
@EnableDubboConfig
@EnableApolloConfig
@EnableScheduling
@Import(HealthController.class)
@EnableDiscoveryClient
public class BGServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BGServiceApplication.class, args);
    }

}
