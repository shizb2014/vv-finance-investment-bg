package com.vv.finance.investment.bg.api.f10;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.investment.bg.dto.f10.*;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import com.vv.finance.investment.bg.entity.information.CompanyEventVo;
import com.vv.finance.investment.bg.entity.req.CompanyEventReq;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/8/19 13:47
 */
public interface F10StockInformationApi {

    /**
     * 证券资料
     *
     * @param code 股票代码
     * @return
     */
    ResultT<SecuritiesInformation> securityInfo(String code);

    /**
     * 董事高管
     *
     * @param code
     * @return
     */
    ResultT<List<DirectorManager>> directorManager(String code);

    /**
     * 分页查询董事高管信息
     *
     * @param code
     * @param pageReq
     * @return
     */
    ResultT<PageDomain<DirectorManager>> directorManagerPage(String code, SimplePageReq pageReq);

    /**
     * 分红派息
     *
     * @param req
     * @return
     */
    ResultT<Dividend> dividend(F10PageBaseReq req);

    /**
     * 分红前后涨跌幅变化（近10次）
     *
     * @param code
     * @return
     */
    ResultT<List<Xnhks0112>> changeBeforeAfterDividend(String code);

    /**
     * 同行业分红对比
     *
     * @param req
     * @return
     */
    ResultT<DividendComparison> dividendComparison(F10PageBaseReq req);


    /**
     * 股票回购
     *
     * @param req
     * @return
     */
    ResultT<SimplePageResp<StockRepurchase>> stockRepurchase(F10PageBaseReq req);

    /**
     * 拆股合并
     *
     * @param req
     * @return
     */
    ResultT<SimplePageResp<SplitMerger>> splitMerger(F10PageBaseReq req);


    /**
     * 公司事件
     * @param req
     * @return
     */
    List<CompanyEventVo> hkCompanyEvent(CompanyEventReq req);


}
