package com.vv.finance.investment;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.utils.JsonUtils;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.domain.UserQuotationCycleConfig;
import com.vv.finance.investment.bg.dto.kline.KlineDTO;
import com.vv.finance.investment.bg.dto.req.CustomKlineReq;
import com.vv.finance.investment.bg.enums.UnitEnum;
import com.vv.finance.investment.bg.mapper.quotationconfig.UserQuotationCycleConfigMapper;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author hamilton
 * @date 2020/12/4 15:53
 */
@SpringBootTest(classes = BGServiceApplication.class)
public class RankTest {

    @Resource
    StockRankingApi stockRankingApi;
    @Resource
    UserQuotationCycleConfigMapper userQuotationCycleConfigMapper;
    @Resource
    IStockKlineService iStockKlineService;

    @Test
    public void test(){
        ResultT<StockIndustryDto> stockIndustryDtoResultT = stockRankingApi.queryStockIndustry("00702.hk");
        System.out.println(stockIndustryDtoResultT);

        ResultT<StockIndustryDto> stockIndustryDtoResultT1 = stockRankingApi.queryStockIndustry("00702.hk");
        System.out.println(stockIndustryDtoResultT1);
    }

    @Test
    public void test2(){
        UserQuotationCycleConfig userQuotationCycleConfig = userQuotationCycleConfigMapper.selectById(1);
        System.out.println(userQuotationCycleConfig);
    }

    @Test
    public void test3(){
        CustomKlineReq customKlineReq = new CustomKlineReq();
        customKlineReq.setCode("HY100506.hk");
        customKlineReq.setAdjhkt("not");
        customKlineReq.setType("day");
        customKlineReq.setUnit(UnitEnum.DAY);
        customKlineReq.setNum(5);
        customKlineReq.setCalculateBuySellPoint(true);

//        KlineDTO klineDTO = iStockKlineService.customQueryAllKline(customKlineReq);
//        System.out.println("获取到结果："+JsonUtils.beanToJson(klineDTO));
    }

}
