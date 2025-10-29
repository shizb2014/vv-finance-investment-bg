package com.vv.finance.investment;

import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.quotation.ITechnicalIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

/**
 * @Description
 * @Author liuxing
 * @Create 2024/5/30 10:08
 */
@SpringBootTest(classes = BGServiceApplication.class)
@Slf4j
public class HkTechnicalIndicatorTest {

    @Resource
    private ITechnicalIndicatorService technicalIndicatorService;

    @Test
    void getTechnicalIndicatorMap(){
        try {
//            Thread.sleep(30000L);
            Collection<String> codes = Arrays.asList("00700.hk");
            Integer limit =60;
            Integer duration = 0;
            long localtimeStamp = 1711900800000L;
            technicalIndicatorService.getTechnicalIndicatorMap(codes,limit,duration, localtimeStamp);
            Thread.sleep(90000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
