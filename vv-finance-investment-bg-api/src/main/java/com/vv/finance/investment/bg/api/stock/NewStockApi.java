package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.common.ComNewStockInfoVo;
import com.vv.finance.common.entity.common.ComNewStockProspectusVo;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.newcode.resp.NewStockListResp;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author liuxing
 * @Create 2023/6/29 14:24
 */
public interface NewStockApi {
    /**
     * 获取全部新股code列表
     * @param sort
     * @param sortKey
     * @return
     */
    ResultT<List<StockCodeNameBaseDTO>> getNewStockCodeList(String sort, String sortKey);

    /**
     * 获取新股列表
     * @param stockCodeList
     * @return
     */
    ResultT<List<NewStockListResp>> getNewStockInfoList(String[] stockCodeList);

    /**
     * 获取新股昨收价
     * @return
     */
    Map<String, BigDecimal> getNewStockPrice();
    /**
     * 获取新股上市信息
     * @param marketStatus 状态 0:认购中 1:待公布中签 2:公布中签 3:待上市
     * @return
     */
    List<ComNewStockProspectusVo> getNewStockProspectusList(Integer marketStatus);
    /**
     * 获取新股信息列表
     * @param stockCodes
     * @return
     */
    List<ComNewStockInfoVo> getNewStockInfos(Set<String> stockCodes);
}
