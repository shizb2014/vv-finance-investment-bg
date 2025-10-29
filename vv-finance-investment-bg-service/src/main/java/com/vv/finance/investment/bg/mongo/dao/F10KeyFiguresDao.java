package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.entity.common.PageReq;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.financial.F10KeyFiguresFinancial;
import com.vv.finance.investment.bg.mongo.model.*;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/7/20 11:20
 */
public interface F10KeyFiguresDao {

    /**
     * 分页查询金融指标
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10KeyFiguresFinancialEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq);
    /**
     * 分页查询非金融指标
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10KeyFiguresNonFinancialEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq);

    /**
     * 分页查询保险
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10KeyFiguresInsuranceEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq);

    List<F10NoFinProfitEntity> listNonFinancial(String id, int size, Long startTime);

    List<F10FinProfitEntity> listFinancial(String id, int size, Long startTime);

    List<F10InsureProfitEntity> listInsurance(String id, int size, Long startTime);

}
