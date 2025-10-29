package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenlibao.security.sdk.ws.core.model.resp.MarketStatisticsResp;
import com.google.common.collect.Lists;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.constants.GlobalConstants;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.StockIndexConstants;
import com.vv.finance.common.entity.common.CommonTradeCapital;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.dto.ComRankShortSale;
import com.vv.finance.common.entity.quotation.common.ComDDENetVo;
import com.vv.finance.common.entity.quotation.common.ComSceneReq;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.enums.StockRelationBizEnum;
import com.vv.finance.common.us.constants.UsRedisKeyConstants;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.frontend.v2.StockServiceV2;
import com.vv.finance.investment.bg.api.stock.StockApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.StockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.info.*;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.dto.req.RtKlineReq;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockLastAndLotSize;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import com.vv.finance.investment.bg.dto.stock.StockSecurityStatus;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.trade.TradeStatisticsDetail;
import com.vv.finance.investment.bg.entity.uts.Xnhk0603;
import com.vv.finance.investment.bg.handler.stock.StockHandler;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.dto.StockIndexDetail;
import com.vv.finance.investment.bg.stock.info.dto.StockShortSale;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.info.service.IStockRelatedDetailsService;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName StockApiImpl
 * @Deacription 股票相关api
 * @Author lh.sz
 * @Date 2021年09月17日 14:47
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class StockApiImpl implements StockApi {
    @Resource
    RedisClient redisClient;
    @Resource
    StockDefineMapper stockDefineMapper;
    @Resource
    IStockDefineService stockDefineService;
    @Resource
    StockService stockService;
    @Resource
    StockServiceV2 stockServiceV2;

    @Resource
    StockHandler stockHandler;
    @Resource
    IStockRelatedDetailsService stockRelatedDetailsService;

    @Resource
    private StockCache stockCache;

    /**
     * 指数跑马灯
     */
    private static final String[] INDEX_CODES = {StockIndexConstants.HS, StockIndexConstants.GQ, StockIndexConstants.HC};

    /**
     * @param code 股票代码
     * @return
     */
    @Override
    public ResultT<StockLastAndLotSize> getLastAndLotSize(String code) {
        StockLastAndLotSize stockLastAndLotSize = StockLastAndLotSize.builder().build();
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        if (snapshot != null) {
            stockLastAndLotSize.setLotSize(snapshot.getLotSize() == null ? null : snapshot.getLotSize());
            stockLastAndLotSize.setLast(snapshot.getLast() == null ? null : snapshot.getLast());
        }
        return ResultT.success(stockLastAndLotSize);
    }

    /**
     * @param codes 股票集合
     * @return
     */
    @Override
    public ResultT<List<StockSecurityStatus>> getStockSecurityStatus(String... codes) {
        List<StockSecurityStatus> stockSecurityStatuses = new ArrayList<>();
        List<StockDefine> objects = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().
                select("code", "suspension").in("code", codes));
        if (CollectionUtils.isNotEmpty(objects)) {
            objects.forEach(s -> {
                StockSecurityStatus stockSecurityStatus = new StockSecurityStatus();
                stockSecurityStatus.setStockCode(s.getCode());
                stockSecurityStatus.setSuspension(s.getSuspension());
                stockSecurityStatuses.add(stockSecurityStatus);
            });
        }
        return ResultT.success(stockSecurityStatuses);
    }

    /**
     * 获取所有得行业数据
     *
     * @return ResultT
     */
    @Override
    public ResultT<List<IndustrySubsidiary>> getAllIndustry() {
        return ResultT.success(stockService.getAllIndustry());
    }

    /**
     * 获取股票列表
     *
     * @param industryCode 市场代码
     * @param sort         排序
     * @param sortKey      排序字段
     * @param market       市场
     * @return
     */
    @Override
    public ResultT<List<StockBaseInfoDTO>> getStockInfo(String industryCode,
                                                        String sort,
                                                        String sortKey,
                                                        String market) {
        List<StockBaseInfoDTO> stockBaseInfoList = stockService.getStockBaseInfoList(industryCode, sort, sortKey, market);
        //获取每日最新的码表
        Set<String> codeAndNameSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET);

        if (CollectionUtils.isNotEmpty(codeAndNameSet)) {
            Map<String, String> stockCodeMap = codeAndNameSet.stream()
                    .collect(Collectors.toMap(item -> item.split(StrPool.COMMA)[0], item -> item.split(StrPool.COMMA)[1]));
            stockBaseInfoList = stockBaseInfoList.stream().filter(s -> stockCodeMap.containsKey(s.getStockCode())).collect(Collectors.toList());
        }
        return ResultT.success(stockBaseInfoList);
    }

    @Override
    public ResultT<List<StockBaseInfoDTO>> getStockList(String industryCode, String sort, String sortKey, String market) {
        List<StockBaseInfoDTO> stockBaseInfoList = stockService.getStockList(industryCode, sort, sortKey, market);
        //获取每日最新的码表
        Set<String> codeAndNameSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET);

        // 行业成分股不过滤当日码表
        if (CollectionUtils.isNotEmpty(codeAndNameSet) && StrUtil.isBlank(industryCode)) {
            Map<String, String> stockCodeMap = codeAndNameSet.stream()
                    .collect(Collectors.toMap(item -> item.split(StrPool.COMMA)[0], item -> item.split(StrPool.COMMA)[1]));
            stockBaseInfoList = stockBaseInfoList.stream().filter(s -> stockCodeMap.containsKey(s.getStockCode())).collect(Collectors.toList());
        }
        return ResultT.success(stockBaseInfoList);
    }

    @Override
    public ResultT<List<StockSnapshot>> getSnapshotList(String[] stockCodeList) {
        return ResultT.success(stockService.getSnapshotList(stockCodeList));
    }

    @Override
    public ResultT<OrderBrokerDto> getOrderBroker(String code,
                                                  String type) {
        return ResultT.success(stockService.getEconomy(code, type));
    }

    @Override
    public ResultT<List<StockBaseDTO>> getStockSort(String sortKey,
                                                    Integer num) {
        return ResultT.success(stockService.getStockSort(sortKey, num));
    }

    @Override
    public ResultT<List<StockBaseDTO>> getRank5Min(String sortKey, Integer num) {
        return ResultT.success(stockService.getRank5Min(sortKey, num));
    }

    @Override
    public ResultT<MarketStatisticsResp> getMarketStatistics() {
        return ResultT.success(stockService.getMarketStatistics());
    }

    @Override
    public ResultT<List<InduBaseRankDTO>> getIndustryRankDetail() {
        return ResultT.success(stockService.getIndustryRankDetail());

    }

    @Override
    public ResultT<List<StockQueryDTO>> searchStock(String stockCode, boolean isGroup, boolean isPc) {
        return ResultT.success(stockService.queryStock(stockCode, isGroup, isPc));
    }

    @Override
    public ResultT<CapitalDistributionVo> getCapitalDistribution(String stockCode) {
        return ResultT.success(stockServiceV2.getCapitalDistribution(stockCode));
    }

    @Override
    public ResultT<List<DDENetVo>> listDDENet(String stockCode) {
        return ResultT.success(stockServiceV2.listDDENet(stockCode));
    }

    @Override
    public ResultT<List<ComDDENetVo>> listDDENetByTime(String stockCode, Long startTime, Long endTime) {
        return ResultT.success(stockServiceV2.listDDENetByTime(stockCode, startTime, endTime));
    }

    @Override
    public ResultT<List<CommonTradeCapital>> listDDEForQuant(String stockCode, LocalDate date, Integer todayFlag) {
        return ResultT.success(stockServiceV2.listDDEForQuant(stockCode, date, todayFlag));
    }

    @Override
    public ResultT<List<Xnhk0603>> getStockShortSale(String stockCode) {
        return ResultT.success(stockHandler.getStockShortSale(stockCode));
    }

    @Override
    public PageDomain<ComRankShortSale> getRankShortSale(Integer currentPage, Integer pageSize, String sort, String sortKey) {
        return stockHandler.getRankShortSale(currentPage, pageSize, sort, sortKey);
    }

    @Override
    public ResultT<List<NetRankVo>> getNetRank(String code) {
        return ResultT.success(stockServiceV2.getTotalNetRank(code));
    }

    @Override
    public ResultT<Order> getStockOrder(String stockCode) {
        return ResultT.success(stockServiceV2.getStockOrder(stockCode));
    }
    /**
     * 批量获取盘口一档数据
     *
     * @param codes 股票代码集合
     * @return List<Order>
     */
    @Override
    public List<Order> getStockOrderByCodes(List<String> codes) {
        return stockServiceV2.getStockOrderByCodes(codes, 1);

    }
    /**
     * 批量获取盘口全部数据
     *
     * @param codes 股票代码集合
     * @return List<Order>
     */
    @Override
    public List<Order> getStockOrderAllByCodes(List<String> codes) {
        return stockServiceV2.getStockOrderByCodes(codes,-1);
    }

    @Override
    public ResultT<List<StockIndexDetail>> getIndexHorseRaceLamp() {
        List<StockIndexDetail> stockIndexDetails = new LinkedList<>();
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        Arrays.stream(INDEX_CODES).forEach(s -> {
            StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(s));
            StockIndexDetail indexDetail = StockIndexDetail.builder().build();
            if (snapshot != null) {
                indexDetail.setTime(snapshot.getTime());
                indexDetail.setIndexCode(snapshot.getCode());
                indexDetail.setIndexName(stockNameMap.get(snapshot.getCode()));
                indexDetail.setLast(snapshot.getLast());
                indexDetail.setChg(snapshot.getChg());
                indexDetail.setChgPct(snapshot.getChgPct());
                indexDetail.setStockId(snapshot.getStockId());
            }
            stockIndexDetails.add(indexDetail);
        });
        return ResultT.success(stockIndexDetails);
    }

    @Override
    public ResultT<Boolean> saveStockRelated(StockRelatedDetails relatedDetails) {
        return ResultT.success(stockRelatedDetailsService.saveOrUpdate(relatedDetails,
                new QueryWrapper<StockRelatedDetails>().eq("code", relatedDetails.getCode())));
    }

//    @Override
//    public ResultT<String> repairStocksDetailSnapshot(List<String> stocks) {
//        try {
//            stockRelatedDetailsService.repairStocksDetailSnapshot(stocks);
//            return ResultT.success();
//        } catch (Exception e) {
//            log.error("[repairStocksDetail]异常", e);
//            return ResultT.fail();
//        }
//    }

    @Override
    public ResultT<List<StockDefine>> getStockDefineList() {
        return ResultT.success(stockDefineService.listStockColumns(null));
    }

    @Override
    public ResultT<Void> updateSnapshotSuspension(int suspension) {
        try {
            stockRelatedDetailsService.updateSnapshotSuspension(suspension);
            return ResultT.success();
        } catch (Exception e) {
            log.error("[stockRelatedDetailsService]异常", e);
            return ResultT.fail();
        }
    }

    @Override
    public Boolean delDdeBySizeCriterion(String stockCode, long time) {
        stockServiceV2.delDdeBySizeCriterion(stockCode, time);
        return Boolean.TRUE;
    }
}
