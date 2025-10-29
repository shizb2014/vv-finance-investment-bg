package com.vv.finance.investment;

import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.lineshape.LineshapeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Auther: GMC
 * @Date: 2024/6/28 11:43
 * @Description:
 * @version: 1.0
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class LineshapeServiceTest {
    @Autowired
    private LineshapeService lineshapeService;
    @Test
    public void delLineshapeByStockCodeTest(){
        lineshapeService.delLineshapeByStockCode("02906.hk");

    }
}
