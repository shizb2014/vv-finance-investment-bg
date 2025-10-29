package com.vv.finance.investment.broker;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.impl.HkTradingCalendarApiImpl;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0102Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0127Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0610Mapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerMarketValueStatisticsMapper;
import com.vv.finance.investment.bg.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 经纪商需求测试类
 *
 * @Auther: shizhibiao
 * @Date: 2022/10/21
 * @Description: com.vv.finance.investment.broker
 * @version: 1.0
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class InitBrokerAnalysisTest {


}
