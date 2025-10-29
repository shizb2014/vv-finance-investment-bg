package com.vv.finance.investment.bg.api.frontend;

import com.fenlibao.security.sdk.ws.core.model.resp.IndhktryResp;
import com.fenlibao.security.sdk.ws.core.model.resp.MarketStatisticsResp;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.dto.ComSearchStockDto;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.common.ComStockBaseDTO;
import com.vv.finance.common.entity.quotation.common.ComStockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.StockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.info.InduBaseRankDTO;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockKlineDTO;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import com.vv.finance.investment.bg.dto.stock.StockRtKlineDTO;
import com.vv.finance.investment.bg.entity.req.KlineQueryReq;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author chenyu
 * @date 2020/10/27 11:56
 */
public interface StockService {

    /**
     * 获取股票代码和股票名字列表
     *
     * @param industryCode
     * @param sort
     * @param sortKey
     * @param market
     * @return
     */
    List<StockBaseInfoDTO> getStockBaseInfoList(String industryCode,
                                                String sort,
                                                String sortKey,
                                                String market);

    /**
     * 市场获取股票列表
     *
     * @param industryCode 市场代码
     * @param sort         排序
     * @param sortKey      排序字段
     * @param market       市场
     * @return {@link List}<{@link StockBaseInfoDTO}>
     */
    List<StockBaseInfoDTO> getStockList(String industryCode, String sort, String sortKey, String market);

    /**
     * 市场获取股票列表(包含退市股票)
     *
     * @param sort         排序
     * @param sortKey      排序字段
     * @return {@link List}<{@link StockBaseInfoDTO}>
     */
    List<StockBaseInfoDTO> getStockListIncludeClose(String sort, String sortKey);

    /**
     * 股票排序
     *
     * @param codes   股票代码
     * @param sort    排序
     * @param sortKey 排序字段
     * @return {@link List}<{@link StockBaseInfoDTO}>
     */
    List<StockBaseInfoDTO> getSortStockList(List<String> codes, String sort, String sortKey);


    /**
     * 获取快照列表
     *
     * @param stockCodeList 股票代码集合
     * @return
     */
    List<StockSnapshot> getSnapshotList(String[] stockCodeList);

    List<StockSnapshot> getSnapshotListBySet(Set<String> stockCodeList);

    /**
     * 获取快照列表 带排序
     *
     * @param stockCodeList 股票代码集合
     * @return
     */
    List<StockSnapshot> getSnapshotListSort(Set<String> stockCodeList, String sortKey, String sort);
    /**
     * 获取单只股票快照
     *
     * @param code
     * @return
     */
    StockSnapshot getOnlyStockSnapshot(String code);


    /**
     * 获取股票排行榜
     *
     * @param sortKey
     * @return
     */
    List<StockBaseDTO> getStockSort(
            String sortKey,
            Integer num
    );

    /**
     * 获取5分钟排行榜
     *
     * @param sortKey
     * @param num
     * @return
     */
    List<StockBaseDTO> getRank5Min(
            String sortKey,
            Integer num
    );


    /**
     * 获取大盘统计数据
     *
     * @return
     */
    MarketStatisticsResp getMarketStatistics();

    /**
     * 领涨行业详情
     *
     * @return
     */
    List<InduBaseRankDTO> getIndustryRankDetail();


    /**
     * 根据不同类型获取K线图
     *
     * @return
     */
//    StockKlineDTO getStockKLine(KlineQueryReq klineQueryReq) throws ParseException, ExecutionException, InterruptedException;

    /**
     * 获取实时k线图
     *
     * @param stockCode
     * @return
     */
//    StockRtKlineDTO getRtStockKline(String stockCode, String type) throws ExecutionException, InterruptedException;

//    /**
//     * 资金流转
//     *
//     * @param stockCode
//     * @return
//     */
//    List<HkCapitalFlowResp> getCapitalFlow(String stockCode);

    /**
     * 获取经济席位
     *
     * @param code
     * @param type
     * @return
     */
    OrderBrokerDto getEconomy(String code, String type);


    /**
     * 行业明细
     *
     * @param stockCode
     * @return
     */
    IndhktryResp getIndhktry(String stockCode);


    /**
     * 搜索股票
     *
     * @param key
     * @return
     */
    List<StockQueryDTO> queryStock(String key, boolean isGroup, boolean isPc);

    /**
     * 搜索股票
     *
     * @param key
     * @return
     */
    List<ComSearchStockDto> queryStockV2(String key, Integer stockType, boolean isGroup, boolean isPc);

    /**
     * 搜索股票
     *
     * @param key
     * @return
     */
    List<ComSearchStockDto> queryStockV3(String key, String stockType, boolean isGroup, boolean isPc);


    /**
     * 获取所有行业板块
     */
    List<IndustrySubsidiary> getAllIndustry();

    /**
     * 获取所有股票
     *
     * @return
     */
    PageDomain<StockSnapshot> getAllStock(Integer current, Integer size, String sort, String sortKey);

    /**
     * 获取相关行业所有股票
     *
     * @return
     */
    PageDomain<StockSnapshot> getStockByIndustry(Integer current, Integer size, String code, String sort, String sortKey);


    /**
     * 获取所有码表
     *
     * @return
     */
    Set<String> getAllStockCode();

    /**
     * 获取所有的股票代码和名称
     *
     * @return
     */
    Map<String, String> getAllStockCodeAndName();

    /**
     * 获取股票码表
     *
     * @param code 股票代码
     * @return
     */
    StockDefine getStockDefine(String code);

    /**
     * 获取历史记录搜索框数据
     *
     * @param stockHistory
     * @return
     */
    List<StockQueryDTO> queryStockByHistory(List<String> stockHistory);

    /**
     * 获取历史记录搜索框数据
     *
     * @param stockHistory
     * @return
     */
    List<ComSearchStockDto> queryStockByHistoryV2(List<String> stockHistory);

    /**
     * 搜索股票分页、排序、搜索
     *
     * @param key
     * @return
     */
    PageDomain<StockQueryDTO> queryStockByPage(String key,Long current, Long size);

    /**
     * 搜索股票不分页、排序
     *
     * @param key
     * @return
     */
    List<StockQueryDTO> queryStockSort(String key);

    /**
     * 根据股票类型查询股票信息
     *
     * @param type
     * @param sort
     * @param sortKey
     * @return
     */
    List<ComStockBaseInfoDTO> gatStockByType(Integer type, String sort, String sortKey);

    /**
     * 获取板块成分股快照
     *
     * @param blockCode 板块代码
     * @return {@link List}<{@link StockSnapshot}>
     */
    List<StockSnapshot> getStockSnapshotByBlockCode(String blockCode);

    /**
     * 删除临时股票快照数据
     */
    void delSnapshotByStockCode(String stockCode);

    /**
     * 变更快照数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void updateSnapshotStockCode(String sourceCode, String targetCode);

    /**
     * 变更快照数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void copySnapshotStockCode(String sourceCode, String targetCode);

    ResultT<Map<String, BigDecimal>> queryStockVol();

    ResultT<Map<String, Integer>> queryTradeNum();
}
