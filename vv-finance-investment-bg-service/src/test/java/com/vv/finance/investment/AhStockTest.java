package com.vv.finance.investment;

import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.southward.SouthwardCapitalApi;
import com.vv.finance.investment.bg.api.stock.AhStockApi;
import com.vv.finance.investment.bg.dto.southward.req.SouthwardCapitalTrendReq;
import com.vv.finance.investment.bg.enums.SouthwardCapitalDateTypeEnum;
import com.vv.finance.investment.bg.job.southward.SouthwardCapitalJob;
import com.vv.finance.investment.bg.job.stock.AhStockJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/7/4
 * @Description:
 */
@SpringBootTest(classes = BGServiceApplication.class)
@Slf4j
public class AhStockTest {
    @Resource
    private AhStockJob ahStockJob;

    @Resource
    private SouthwardCapitalJob southwardCapitalJob;

    @Resource
    private AhStockApi ahStockApi;

    @Resource
    private SouthwardCapitalApi southwardCapitalApi;



    @Test
    public void saveAhStockRedisJob() {
        ahStockJob.saveAhStockRedisJob(null);
    }

    @Test
    public void queryAhStockCodeList() {
        ahStockApi.queryAhStockCodeList("desc","");
    }

    @Test
    public void queryAhStockDetailList() {
        List<String> stockCodeList = new ArrayList<>();
        ahStockApi.queryAhStockDetailList(stockCodeList);
    }

    @Test
    public void saveSouthwardTrendForTodayJob() {
        //southwardCapitalJob.saveSouthwardTrendForTodayJob(null);
    }


    @Test
    public void querySouthwardCapitalTrend() {
        SouthwardCapitalTrendReq southwardCapitalTrendReq = new SouthwardCapitalTrendReq();
        southwardCapitalTrendReq.setDateType(1);
        southwardCapitalTrendReq.setTrendType(1);
        southwardCapitalApi.querySouthwardCapitalTrend(southwardCapitalTrendReq);
    }

}