package com.vv.finance.investment.bg.job.uts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.stock.f10.service.IStockHolderChangeService;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.gateway.us.api.stock.IStockBusinessUsApi;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName F10IndustryJob
 * @Deacription 行业对比job
 * @Author lh.sz
 * @Date 2021年08月23日 10:35
 **/
@Component
@Slf4j
public class F10InformationJob {

    @Resource
    RedisClient redisClient;
    @Resource
    MongoTemplate mongoTemplate;
    @DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
    HkTradingCalendarApi tradingCalendarApi;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private IStockBusinessUsApi stockBusinessUsApi;

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private IStockHolderChangeService stockHolderChangeService;

    @XxlJob(value = "updateStockHolderChange", author = "杨鹏", desc = "股东持股占比更新", cron = "0 0 8,20 * * ?")
    public ReturnT<String> updateStockHolderChange(String param) {

        // List<StockDefine> stockDefines = stockDefineService.list();
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);

        if (CollUtil.isEmpty(stockDefines)) {
            return ReturnT.SUCCESS;
        }

        List<String> codeList = stockDefines.stream().map(StockDefine::getCode).collect(Collectors.toList());

        for (String code : codeList) {
            try {
                stockHolderChangeService.updateStockHolderChangeByCode(code);
            } catch (Exception e) {
                log.error("updateStockHolderChange occurs error, code: {}", code, e);
            }
        }

        return ReturnT.SUCCESS;
    }

}
