package com.vv.finance.investment.bg.api.impl.lineshape;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Lists;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.IndexComponentDistribute;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.api.quotation.IQuotationService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.finance.common.constants.RedisKeyConstants.*;

/**
 * @ClassName: QuotationServiceImpl
 * @Description:
 * @Author: Demon
 * @Datetime: 2021/5/31   16:26
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class QuotationServiceImpl implements IQuotationService {

    @Resource
    private RedisClient redisClient;

    @Resource
    private StockCache stockCache;

    @Override
    public Set<String> selectStockNameList() {
        return redisClient.get(RECEIVER_NEWEST_STOCK_CODE_NAME_SET);
    }

    @Override
    public Set<String> selectStockCodeList() {
        return redisClient.get(RECEIVER_NEWEST_STOCK_CODE_SET);
    }

    @Override
    public Map<String, String> selectCompressStockMap() {
        return redisClient.hmget(COMPRESS_STOCK_MAP);
    }

    @Override
    public StockSnapshot selectStockSnapshot(String code) {
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        StockSnapshot snapshot =  redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        if(ObjectUtils.isEmpty(snapshot)){
            log.info("股票{}没有查询到快照", code);
        }else{
            snapshot.setName(stockNameMap.get(snapshot.getCode()));
        }
        return snapshot;
    }

    @Override
    public Order selectOrder(String code) {
        return redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BEAN.concat(code));
    }

    @Override
    public IndexComponentDistribute selectIndexComponentDistribute(String code) {
        return redisClient.hget(RedisKeyConstants.BG_INDEX_COMPONENT_DISTRIBUTE_MAP, code);
    }

    @Override
    public void quotationPush(String pushKey, Object message) {
        redisClient.convertAndSend(pushKey, message);
    }

    @Override
    public List<StockSnapshot> getAllStockSnapshot() {

        Map<Object, Object> stockSnapshotMap = redisClient.hmget(RedisKeyConstants.COMPRESS_STOCK_MAP);
        List<StockSnapshot> snapshotList = Lists.newArrayList();
        stockSnapshotMap.forEach((k, v) -> {
            String val = v.toString();
            String snapshotStr = ZipUtil.gunzip(val);
            StockSnapshot stockSnapshot = JSON.parseObject(snapshotStr, StockSnapshot.class);
            if (stockSnapshot.getCode().endsWith(".hk")) {
                snapshotList.add(stockSnapshot);
            }
        });
        return snapshotList;


    }

    @Override
    public Map<String, Object> getTodayStockSnapshotMap() {
        //获取当日码表数据
        Set<String> allStock =selectStockNameList();
        //查询码表股票
        Map<String, String> stockCodeMap = allStock.stream()
                .collect(Collectors.toMap(item -> item.split(",")[0], item -> item.split(",")[1]));
        //获取当日快照数据
        Map<String, String> snapShotMap = selectCompressStockMap();
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        Set<String> codeSet = stockCodeMap.keySet();
        //根据当日码表数据封装快照数据
        Map<String, Object> snMap = codeSet.parallelStream().map(code -> {

            StockSnapshot stockSnapshot = null;
            try {
                stockSnapshot = JsonUtils.toBean(ZipUtil.gunzip(snapShotMap.get(code)), StockSnapshot.class);
                if (ObjectUtils.isEmpty(stockSnapshot)) {
                    stockSnapshot = null;
                } else {
                    stockSnapshot.setName(stockNameMap.get(stockSnapshot.getCode()));
                }
            } catch (Exception e) {
                log.warn("策略选股获取快照信息错误,code:{}", code, e);
            }
            return stockSnapshot;
        }).filter(ObjectUtils::isNotEmpty).collect(Collectors.toMap(StockSnapshot::getCode, Function.identity()));

        return snMap;
    }


}
