package com.vv.finance.investment.SpecialTest;

import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.frontend.StockService;
//import com.vv.finance.investment.bg.api.special.SpecialJobApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
//import com.vv.finance.investment.bg.job.special.SpecialJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/11/23 16:34
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class SpecialJobTest {

//    @Resource
//    SpecialJob specialJob;
//
//    @Resource
//    SpecialJobApi specialJobApi;

//    @Test
//    public void testDealParallelCodeCurrentDay(){
//        specialJob.dealParallelCodeCurrentDay("20240731");
//    }
//
//    @Test
//    public void dealParallelCodeNextDay(){
//        specialJob.dealParallelCodeNextDay("20240726");
//    }
//
//    @Test
//    public void dealReuseCode(){
//        specialJob.dealReuseCode("20240731");
//    }
//
//    @Test
//    public void dealChangeCode(){
//        specialJob.dealChangeCode("20240807");
//    }
}
