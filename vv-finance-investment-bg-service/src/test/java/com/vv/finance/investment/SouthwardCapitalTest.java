package com.vv.finance.investment;

import cn.hutool.json.JSONUtil;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.quotation.ITechnicalIndicatorService;
import com.vv.finance.investment.bg.api.southward.SouthwardCapitalApi;
import com.vv.finance.investment.bg.dto.kline.KlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.job.southward.SouthwardCapitalJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qinxi
 * @date 2023/6/29 11:34
 * @description: 南向资金单元测试类
 */
@SpringBootTest(classes = BGServiceApplication.class)
@Slf4j
public class SouthwardCapitalTest {

    @Resource
    private SouthwardCapitalJob southwardCapitalJob;

    @Resource
    private SouthwardCapitalApi southwardCapitalApi;

    @Resource
    private IStockKlineService stockKlineService;

    @Resource
    private ITechnicalIndicatorService technicalIndicatorService;


    @Test
    public void saveStockSouthwardCapitalJob() {
        southwardCapitalJob.saveStockSouthwardCapitalJob("0,2");
    }

    @Test
    public void saveSouthwardCapitalStockDetailToRedisJob() {
        southwardCapitalJob.saveSouthwardCapitalStockDetailToRedisJob("");
    }

    @Test
    public void saveSouthwardCapitalJob() {
        southwardCapitalJob.saveSouthwardCapitalJob("60");
    }

//    @Test
//    public void saveSouthwardCapitalToRedisJob() {
//        southwardCapitalJob.saveSouthwardCapitalToRedisJob("60");
//    }

    @Test
    public void querySouthwardCapitalList() {
        southwardCapitalApi.querySouthwardCapitalList();
    }

    @Test
    public void querySouthwardCapitalStockDetail() {
        southwardCapitalApi.querySouthwardCapitalStockDetail(null);
    }


    @Test
    public void querySouthwardCapitalStockList() {
        southwardCapitalApi.querySouthwardCapitalStockList("desc", "netTurnoverIn");
    }

    @Test
    public void saveCapitalInfoForTodayMinuteJob() {
//        southwardCapitalJob.saveCapitalInfoForTodayMinuteJob(null);
    }
    @Test
    public void getExceptionNetBuyingTurnoverResp() {
        LocalDateTime currentDateTime = LocalDateTime.now().withSecond(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String today = currentDateTime.format(formatter);

        long currentDateLong = LocalDateTime.parse(today, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.of("+8")).toEpochMilli();

//        southwardCapitalJob.getExceptionNetBuyingTurnoverResp(currentDateLong, LocalDate.now());
    }


    @Test
    public void testKline() {
        KlineReq req = new KlineReq();
        req.setCode("06812.hk");
        req.setAdjhkt("forward");
//        req.setType("month");
        req.setCalculateBuySellPoint(false);
        req.setIndicators(null);
        req.setCurrent(1);
        req.setPageSize(20);
        req.setEndTime(System.currentTimeMillis());
//        KlineDTO klineDTO = stockKlineService.queryAllKline(req);
//        System.out.println(JSONUtil.toJsonPrettyStr(klineDTO));
        req.setType("year");
//        KlineDTO klineDTO = stockKlineService.queryAllKline(req);
//        System.out.println(JSONUtil.toJsonPrettyStr(klineDTO));
    }
}
