package com.vv.finance.investment.bg.api.frontend;

import com.fenlibao.security.sdk.ws.core.model.resp.TrendResp;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.index.IndexDashBoardInfo;
import com.vv.finance.investment.bg.dto.info.IndustrySectorDTO;
import com.vv.finance.investment.bg.dto.stock.IndexConstituent;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockRtKlineDTO;

import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/29 10:59
 */
public interface IndexService {

    /**
     * 获取行业板块信息
     *
     * @param num
     * @return
     */
    List<IndustrySectorDTO> getIndustrySector(Integer num);


    /**
     * 获取分时k线接口
     *
     * @param stockCode
     * @param type
     * @return
     */
    StockRtKlineDTO getRtStockKline(String stockCode, String type);


    /**
     * 获取指数成分股
     *
     * @param code
     * @param pageSize
     * @param sort
     * @param sortKey
     * @return
     */
    List<StockBaseDTO> getIndexComponent(String code,
                                         Integer pageSize,
                                         String sort,
                                         String sortKey);

    /**
     * 获取所有指数
     *
     * @return
     */
    List<IndexDashBoardInfo> getIndexList();

    /**
     * 获取最新分时的一条
     *
     * @param code
     * @return
     */
    List<TrendResp> getLastTrend(String code);

    /**
     * 获取指数成分股股票代码
     *
     * @param indexCode 指数代码
     * @param sortKey   排序字段
     * @param sort      升序降序 up升序 down降序
     * @return
     */
    ResultT<List<IndexConstituent>> getIndexComponentCode(String indexCode, String sortKey, String sort);
}
