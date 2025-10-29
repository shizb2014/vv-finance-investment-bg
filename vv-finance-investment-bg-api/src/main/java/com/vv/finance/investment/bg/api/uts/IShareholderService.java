package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.investment.bg.dto.f10.MainShareholdingReqDTO;
import com.vv.finance.investment.bg.dto.f10.StockHolderChangeReqDTO;
import com.vv.finance.investment.bg.entity.f10.shareholder.*;
import com.vv.finance.investment.bg.entity.information.PageWithStockHolder;

import java.util.List;

/**
 * @Author: wsliang
 * 股本信息
 * @Date: 2021/9/1 10:06
 **/
public interface IShareholderService {

    /**
     * 持股占比变化
     * @param code
     * @return
     */
    PopChanges getPopChanges(String code);

    /**
     * 持股占比变化
     * @param code
     * @return
     */
    StockHolderPopChange getPopChangesV2(String code);

    /**
     * 持股占比变化
     * @param code
     * @return
     */
    StockPopAndChange getStockPopAndChanges(String code, Integer quaSize);

    /**
     * 股东统计-持股占比(饼图)
     * @param stockCode
     * @return
     */
    ShareholdingPop percentageByType(String stockCode);

    /**
     * 股东统计-持股占比(饼图)
     * @param stockCode
     * @return
     */
    StockHolderPop percentageByTypeV2(String stockCode);

    /**
     * 股东统计-主要股东(pc)
     * @return
     */
    PageDomain<MainShareholding> getMainShareholding(MainShareholdingReqDTO mainShareholdingReqDTO);

    /**
     * 股东统计-主要股东(pc)
     * @return
     */
    PageWithStockHolder<StockHolder> getMainShareholdingV2(MainShareholdingReqDTO mainShareholdingReqDTO);

    /**
     * @param stockCode
     * @param setShare
     * @param stockHolders
     */
    List<StockHolder> buildStockHolderList(String stockCode, boolean setShare, boolean formatDate, boolean addOther, List<StockHolder> stockHolders);

    /**
     * 股东统计-主要股东（app）
     * @param stockCode
     * @param simplePageReq
     * @return
     */
    PageDomain<MainShareholding> getMainShareholding(String stockCode, SimplePageReq simplePageReq);

    /**
     * 股东统计-股权变动
     * @param stockCode
     * @param simplePageReq
     * @return
     */
    PageDomain<EquityChange> listEquityChanges(String stockCode, SimplePageReq simplePageReq);

    /**
     * 股东统计-股权变动2
     * @return
     */
    PageDomain<EquityChange> listEquityChangesV2(StockHolderChangeReqDTO stockHolderChangeReqDTO);
    List<StockHoldingRiseFall> getTotalStockHolderChangeAmount(String stockCode, Long startQua, Long endQua);
}
