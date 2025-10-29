package com.vv.finance.investment.bg.stock.info.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.OmdcCommonConstant;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.mapper.StockRelatedDetailsMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.info.service.IStockRelatedDetailsService;
import com.vv.finance.common.calc.hk.entity.StockKline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @ClassName StockRelatedDetailsServiceImpl
 * @Deacription
 * @Author lh.sz
 * @Date 2021年12月27日 11:50
 **/
@Service
@DS("db1")
@Slf4j
public class StockRelatedDetailsServiceImpl extends ServiceImpl<StockRelatedDetailsMapper, StockRelatedDetails> implements IStockRelatedDetailsService {

//    @DubboReference(group = "${dubbo.investment.composite.service.group:composite}", registry = "compositeservice")
//    private HkStockCompositeApi hkStockCompositeApi;

    @Autowired
    private IStockDefineService stockDefineService;
    @Autowired
    private RedisClient redisClient;
    @Resource
    private StockCache stockCache;

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void repairStocksDetailSnapshot(List<String> stocks) {
//        log.info("修复股票详情:{}", stocks);
//        if (CollectionUtils.isEmpty(stocks)) {
//            log.warn("[repairStocksDetailSnapshot]入参为空");
//            return;
//        }
//        Map<String, String> stockCodeMap = stockCache.queryStockNameMap(null);
//        // Map<String, String> stockCodeMap = stockDefineService.list(new QueryWrapper<StockDefine>().in("code", stocks)).stream().collect(Collectors.toMap(StockDefine::getCode, StockDefine::getName));
//        QueryWrapper<StockRelatedDetails> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("code");
//        queryWrapper.in("code", stocks);
//        List<String> dbList = baseMapper.selectList(queryWrapper).stream().map(StockRelatedDetails::getCode).collect(Collectors.toList());
//        StockKlineReq klineReq = new StockKlineReq();
//        long now = System.currentTimeMillis();
//        klineReq.setType(OmdcCommonConstant.DAY);
//        klineReq.setTime(now);
//        klineReq.setNum(1);
//        klineReq.setAdjhkt("not");
//        for (String stock : stocks) {
//            klineReq.setCode(stock);
//            List<StockKline> stockKlines = hkStockCompositeApi.selectStockKlineList(klineReq);
//            Optional<StockKline> first = stockKlines.stream().findFirst();
//            if (!first.isPresent()) {
//                log.info("[repairStocksDetailSnapshot]修复股票详情获取不了数据,股票代码为:{}", stock);
//                return;
//            }
//            StockKline stockKline = first.get();
//            StockSnapshot stockSnapshot = new StockSnapshot();
//            stockSnapshot.setCode(stock);
//            stockSnapshot.setChg(stockKline.getChg());
//            stockSnapshot.setChgPct(stockKline.getChgPct());
//            stockSnapshot.setClose(stockKline.getClose());
//            stockSnapshot.setHigh(stockKline.getHigh());
//            stockSnapshot.setLow(stockKline.getLow());
//            stockSnapshot.setOpen(stockKline.getOpen());
//            stockSnapshot.setPreClose(stockKline.getPreClose());
//            stockSnapshot.setTime(stockKline.getTime());
//            stockSnapshot.setTurnover(stockKline.getTurnover());
//            String name = stockCodeMap.get(stock);
//            if (StringUtils.isNotBlank(name)) {
//                stockSnapshot.setName(name);
//            }
//            StockRelatedDetails entity = new StockRelatedDetails();
//            entity.setCode(stock);
//            entity.setSnapshotDetails(JSON.toJSONString(stockSnapshot));
//            if (dbList.contains(stock)) {
//                baseMapper.updateSnapshotDetailByCode(entity);
//            } else {
//                saveOrUpdate(entity);
//            }
//            redisClient.set(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockSnapshot.getCode()), stockSnapshot);
//            redisClient.hset(RedisKeyConstants.COMPRESS_STOCK_MAP, stockSnapshot.getCode(), ZipUtil.gzip(JSON.toJSONString(stockSnapshot)));
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSnapshotSuspension(int suspension) {

        List<StockRelatedDetails> stockRelatedDetails = baseMapper.selectList(new QueryWrapper<>());

        if (CollectionUtils.isNotEmpty(stockRelatedDetails)) {
            stockRelatedDetails.forEach(s -> {
                StockSnapshot snapshot = JSON.parseObject(s.getSnapshotDetails(), StockSnapshot.class);
                snapshot.setSuspension(suspension);
                s.setSnapshotDetails(JSON.toJSONString(snapshot));
                baseMapper.updateSnapshotDetailByCode(s);
            });
        }
    }

}
