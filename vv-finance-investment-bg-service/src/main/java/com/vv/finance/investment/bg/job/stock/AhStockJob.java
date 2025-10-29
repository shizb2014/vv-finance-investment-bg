package com.vv.finance.investment.bg.job.stock;

import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.stock.AhStockDetailResp;
import com.vv.finance.investment.gateway.api.stock.IAhStockBusinessApi;
import com.vv.finance.investment.gateway.dto.resp.AhStockInfoResp;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author:maling
 * @Date:2023/6/29
 * @Description:获取AH股票列表定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AhStockJob {

    @Resource
    private StockInfoApi stockInfoApi;

    @Resource
    private RedisClient redisClient;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    private IAhStockBusinessApi iAhStockBusinessApi;

    @XxlJob(value = "saveAhStockRedisJob", author = "马玲", desc = "更新AH股redis数据(0/5 * 9-17 ? * *)")
    public ReturnT<String> saveAhStockRedisJob(String param) {
        long start = System.currentTimeMillis();
        log.info("获取AH股列表,开始执行,{}",start);
        ResultT<List<AhStockInfoResp>> result = iAhStockBusinessApi.getAhStockInfoList();
        if (result.getCode() != ResultCode.SUCCESS.code()) {
            log.info("获取AH股列表,AH股列表查询返回失败.result={}", result);
            return ReturnT.SUCCESS;
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            log.info("获取AH股列表,AH股列表查询返回数据为空.result={}", result);
            return ReturnT.SUCCESS;
        }
        List<AhStockInfoResp> stockList = result.getData();
        log.info("获取AH股列表,stockList={}", stockList.size());
        List<String> stockCodeList = stockList.stream().map(s -> s.getCode()).collect(Collectors.toList());
        Map<String, Long> stockCodeIdMap = stockInfoApi.selectStockIdByCodes(stockCodeList);
        List<AhStockDetailResp> list = new ArrayList<>();
        stockList.stream().forEach(stock -> {
            list.add(transferAhStockDetail(stock,stockCodeIdMap));
        });
        log.info("获取AH股列表,list={}", list.size());
        redisClient.set(RedisKeyConstants.SOUTHWARD_AH_STOCK_LIST, list);
        log.info("获取AH股列表,总耗时={}", System.currentTimeMillis() - start);
        return ReturnT.SUCCESS;
        //总耗时54ms
    }

    private AhStockDetailResp transferAhStockDetail(AhStockInfoResp stock,Map<String, Long> stockCodeIdMap) {
        AhStockDetailResp ahStockDetail = new AhStockDetailResp();
        ahStockDetail.setStockId(stockCodeIdMap.get(stock.getCode()));
        ahStockDetail.setCode(stock.getCode());
        ahStockDetail.setName(stock.getName());
        ahStockDetail.setCnPrice(stock.getCnPrice());
        ahStockDetail.setCnEquivPrice(stock.getCnEquivPrice());
        ahStockDetail.setCnChangeRate(stock.getCnChangeRate());
        ahStockDetail.setCnRelChangeRate(stock.getCnRelChangeRate());
        ahStockDetail.setAhPremiumRate(stock.getAhPremiumRate());
        ahStockDetail.setCnChange(stock.getCnChange());
        ahStockDetail.setCnRelChange(stock.getCnRelChange());
        ahStockDetail.setCnSymbol(stock.getCnSymbol());
        ahStockDetail.setStockType(StockTypeEnum.STOCK.getCode());
        ahStockDetail.setRegionType(RegionTypeEnum.HK.getCode());
        return ahStockDetail;
    }
}