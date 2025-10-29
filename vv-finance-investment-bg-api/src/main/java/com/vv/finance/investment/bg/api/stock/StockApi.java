package com.vv.finance.investment.bg.api.stock;

import com.fenlibao.security.sdk.ws.core.model.resp.MarketStatisticsResp;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.common.CommonTradeCapital;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.dto.ComRankShortSale;
import com.vv.finance.common.entity.quotation.common.ComDDENetVo;
import com.vv.finance.common.entity.quotation.common.ComSceneReq;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.investment.bg.dto.StockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.info.*;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.dto.req.RtKlineReq;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockLastAndLotSize;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import com.vv.finance.investment.bg.dto.stock.StockSecurityStatus;
import com.vv.finance.investment.bg.entity.uts.Xnhk0603;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.dto.StockIndexDetail;
import com.vv.finance.investment.bg.stock.info.dto.StockShortSale;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName StockApi
 * @Deacription 股票相关api
 * @Author lh.sz
 * @Date 2021年09月17日 14:46
 **/
public interface StockApi {

    /**
     * 获取股票的最新价和每手股数
     *
     * @param code 股票代码
     * @return
     */
    ResultT<StockLastAndLotSize> getLastAndLotSize(String code);

    /**
     * 获取股票状态
     *
     * @param codes 股票集合
     * @return
     */
    ResultT<List<StockSecurityStatus>> getStockSecurityStatus(String... codes);


    /**
     * 获取股票所有的行业数据
     *
     * @return ResultT
     */
    ResultT<List<IndustrySubsidiary>> getAllIndustry();

    /**
     * 市场获取股票列表
     *
     * @param industryCode 市场代码
     * @param sort         排序
     * @param sortKey      排序字段
     * @param market       市场
     * @return <List<StockBaseInfoDTO>>
     */
    ResultT<List<StockBaseInfoDTO>> getStockInfo(String industryCode,
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
     * @return {@link ResultT}<{@link List}<{@link StockBaseInfoDTO}>>
     */
    ResultT<List<StockBaseInfoDTO>> getStockList(String industryCode, String sort, String sortKey, String market);

    /**
     * 获取股票快照信息
     *
     * @param stockCodeList 股票集合
     * @return ResultT<List < StockSnapshot>>
     */
    ResultT<List<StockSnapshot>> getSnapshotList(String[] stockCodeList);

    /**
     * 获取经纪席位
     *
     * @param code 股票
     * @param type 买卖类型（B-买 ,S-卖）不区分大小写
     * @return ResultT<OrderBrokerDto>
     */
    ResultT<OrderBrokerDto> getOrderBroker(String code, String type);

    /**
     * 获取股票排行
     *
     * @param sortKey 排序字段
     * @param num     条数
     * @return ResultT
     */
    ResultT<List<StockBaseDTO>> getStockSort(String sortKey,
                                             Integer num);


    /**
     * 获取5分钟排行榜
     *
     * @param sortKey 排序字段
     * @param num     条数
     * @return ResultT
     */
    ResultT<List<StockBaseDTO>> getRank5Min(
            String sortKey,
            Integer num
    );

    /**
     * 获取大盘统计数据
     *
     * @return ResultT
     */
    ResultT<MarketStatisticsResp> getMarketStatistics();

    /**
     * 获取行业排行详情
     *
     * @return ResultT
     */
    ResultT<List<InduBaseRankDTO>> getIndustryRankDetail();

    /**
     * 股票搜索
     *
     * @param stockCode 股票code
     * @param isGroup   是否分组
     * @return
     */
    ResultT<List<StockQueryDTO>> searchStock(String stockCode, boolean isGroup, boolean isPc);

    /**
     * 获取资金分布
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    ResultT<CapitalDistributionVo> getCapitalDistribution(String stockCode);

    /**
     * 获取主力资金分布
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    ResultT<List<DDENetVo>> listDDENet(String stockCode);

    /**
     * 获取主力资金分布
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    ResultT<List<ComDDENetVo>> listDDENetByTime(String stockCode, Long startTime, Long endTime);

    /**
     * 计算主力资金近一年（量化）
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    ResultT<List<CommonTradeCapital>> listDDEForQuant(String stockCode, LocalDate date, Integer todayFlag);

    /**
     * 获取沽空数据
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    ResultT<List<Xnhk0603>> getStockShortSale(String stockCode);

    /**
     * 获取沽空排行榜
     *
     * @param currentPage 当前页面
     * @param pageSize    页面大小
     * @param sort        排序 asc-升序, desc-降序
     * @param sortKey     排序字段 shortSaleNum-沽空量, shortSaleChangeRate-沽空变化比例
     * @return ResultT
     */
    PageDomain<ComRankShortSale> getRankShortSale(Integer currentPage, Integer pageSize, String sort, String sortKey);

    /**
     * 获取资金排名
     *
     * @param code
     * @return
     */
    ResultT<List<NetRankVo>> getNetRank(String code);

    /**
     * 获取十档数据
     *
     * @param stockCode 股票代码
     * @return
     */
    ResultT<Order> getStockOrder(String stockCode);

    /**
     * 批量获取盘口一档数据
     *
     * @param codes 股票代码集合
     * @return List<Order>
     */
    List<Order> getStockOrderByCodes(List<String> codes);
    /**
     * 批量获取盘口全部数据
     *
     * @param codes 股票代码集合
     * @return List<Order>
     */
    List<Order> getStockOrderAllByCodes(List<String> codes);
    /**
     * 获取指数跑马灯
     *
     * @return ResultT
     */
    ResultT<List<StockIndexDetail>> getIndexHorseRaceLamp();

    /**
     * 保存股票相关数据
     *
     * @param relatedDetails 股票信息
     * @return
     */
    ResultT<Boolean> saveStockRelated(StockRelatedDetails relatedDetails);

    /**
     * 获取全量码表信息
     *
     * @return
     */
    ResultT<List<StockDefine>> getStockDefineList();

    ResultT<Void> updateSnapshotSuspension(int suspension);
    Boolean delDdeBySizeCriterion(String stockCode, long time);
}
