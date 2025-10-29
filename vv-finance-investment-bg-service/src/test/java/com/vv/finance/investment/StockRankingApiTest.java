package com.vv.finance.investment;

import com.alibaba.fastjson.JSONObject;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hamilton
 * @date 2020/11/7 11:14
 */
@SpringBootTest(classes = BGServiceApplication.class)
@Slf4j
public class StockRankingApiTest {

    @Resource
    StockRankingApi stockRankingApi;

    @Test
    public void test(){
        stockRankingApi.initIndustrySubsidiary();
    }
    @Test
    public void test1(){
        stockRankingApi.initIndustrySubsidiary();
    }
    
    @Test
    public void setStockRankingApiTest(){
        ResultT<StockIndustryDto> stockIndustryDtoResultT = stockRankingApi.queryStockIndustry("00700.hk");
    }

    @Test
    public void alldhk(){
        ResultT<List<IndustrySubsidiary>> listResultT = stockRankingApi.listIndustrySubsidiary();
        System.out.println(JSONObject.toJSONString(listResultT.getData()));
    }
}
