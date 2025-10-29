package com.vv.finance.investment.bg.api.stock;

import com.fenlibao.security.sdk.ws.core.model.req.IndhktryReq;
import com.fenlibao.security.sdk.ws.core.model.req.RankInduReq;
import com.fenlibao.security.sdk.ws.core.model.req.RankMin5Req;
import com.fenlibao.security.sdk.ws.core.model.req.RankReq;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import com.vv.finance.investment.bg.stock.rank.entity.IndustryRanking;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.entity.Stock5minRanking;
import com.vv.finance.investment.bg.stock.rank.entity.StockRanking;

import java.util.List;

/**
 * @author hamilton
 * @date 2020/10/28 11:51
 */
public interface StockRankingApi {

    /**
     * 5分排行榜
     * @param rankMin5Req
     * @return
     */
    @Deprecated
    ResultT<List<Stock5minRanking>> stock5minRanking(RankMin5Req rankMin5Req);

    /**
     * 排行榜
     * @param rankReq
     * @return
     */
    @Deprecated
    ResultT<List<StockRanking>> stockRanking(RankReq rankReq);

    /**
     * 行业排行榜
     * @param rankInduReq
     * @return
     */
    @Deprecated
    ResultT<List<IndustryRanking>> industryRanking(RankInduReq rankInduReq);

    /**
     * 行业明细
     * @param indhktryReq
     * @return
     */
    ResultT<IndustryRanking> industrySubsidiary(IndhktryReq indhktryReq);

    /**
     * 查询股票所属行业
     * @param code 股票代码
     * @return
     */
    ResultT<StockIndustryDto> queryStockIndustry(String code);

    IndustrySubsidiary getIndustrySubsidiary(String code);

    /**
     * 所有行业
     * @return
     */
    ResultT<List<IndustrySubsidiary>> listIndustrySubsidiary();

    /**
     * 存入行业信息初始化
     */
    void initIndustrySubsidiary();

    /**
     * 更新昨收价
     */
    ResultT<?> updateIndustryPreClose(List<IndustrySubsidiary> industrySubsidiaryList);




}
