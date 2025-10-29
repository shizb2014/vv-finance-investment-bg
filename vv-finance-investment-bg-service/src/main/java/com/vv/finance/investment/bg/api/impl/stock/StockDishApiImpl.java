package com.vv.finance.investment.bg.api.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.OmdcCommonConstant;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.investment.bg.api.stock.StockDishApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.stock.StockTrendFollowDTO;
import com.vv.finance.investment.bg.stock.info.mapper.StockSnapshotMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockSnapshotService;
import com.vv.finance.investment.bg.stock.info.service.IStockTradeService;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hqj
 * @date 2020/10/28 11:02
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@RequiredArgsConstructor
@Slf4j
public class StockDishApiImpl implements StockDishApi {
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi iStockBusinessApi;
    @Autowired
    IStockSnapshotService iStockSnapshotService;

    @Autowired
    IStockTradeService iStockTradeService;
    @Autowired
    StockSnapshotMapper stockSnapshotMapper;

    @Autowired
    private RedisClient redisClient;

//    @Override
//    public BigDecimal getClosePriceByDate(
//            String stockCode,
//            Long date
//    ) {
//
//        return hkStockCompositeApi.selectKlineList(StockKlineReq.builder()
//                .adjhkt("").type("day").time(date).num(1).build()).get(0).getClose();
//
//    }

//    @Override
//    public ResultT<Void> saveStockSnapshot(List<HistorySnapshotReq> snapshotReqs) {
//        if (!CollectionUtils.isEmpty(snapshotReqs)) {
//            snapshotReqs.forEach(snapshotReq -> {
//                try {
//                    ResultT<PageInfo<HistorySnapshotResp>> resultT =
//                            iStockBusinessApi.getHistorySnapshotData(snapshotReq);
//                    log.info(resultT.getData().getList().toString());
//                    List<StockSnapshot> snapshotList = new ArrayList<>();
//                    resultT.getData().getList().forEach(stockSnapshot -> {
//                        StockSnapshot snapshot = new StockSnapshot();
//                        BeanUtils.copyProperties(stockSnapshot, snapshot);
//                        log.info(snapshot.toString());
//
//                        snapshotList.add(snapshot);
//                    });
//                    iStockSnapshotService.batchSaveOrUpdate(snapshotList);
//                } catch (Exception e) {
//                    log.error("saveStockSnapshot fail of code :{},e:{}", snapshotReq.getSymbol(), e);
//                }
//            });
//        }
//        return ResultT.success();
//    }


    @Override
    public StockSnapshot queryStockSnapshot(String stockCode) {
        StockSnapshot snapshot =
                redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockCode));
        if (snapshot == null) {
            return stockSnapshotMapper
                    .selectList(new QueryWrapper<StockSnapshot>().eq("code", stockCode).orderByDesc("time")).get(0);
        }
        return snapshot;
    }


    @Override
    public ResultT<BigDecimal> getLastPrice(String code) {
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        if (snapshot == null) {
            snapshot = queryStockSnapshot(code);
        }
        return ResultT.success(snapshot.getLast());
    }

    @Override
    public ResultT<Map<String, BigDecimal>> getLastPriceMap(Set<String> codes) {

        return ResultT.success(redisClient.hmget(RedisKeyConstants.RECEIVER_STOCK_CODE_LAST_MAP));
    }

    @Override
    public ResultT<Map<String, BigDecimal>> getLastPriceMap() {

        return ResultT.success(redisClient.hmget(RedisKeyConstants.RECEIVER_STOCK_CODE_LAST_MAP));
    }

    @Override
    public ResultT<BigDecimal> getPreClose(String code) {
        StockSnapshot snapshot =
                redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        if (snapshot == null) {
            snapshot = queryStockSnapshot(code);
        }
        return ResultT.success(snapshot.getPreClose());
    }
}
