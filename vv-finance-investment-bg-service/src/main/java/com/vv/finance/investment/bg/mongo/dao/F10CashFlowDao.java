package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;

/**
 * @ClassName F10CashFlowDao
 * @Deacription 现金流量表
 * @Author lh.sz
 * @Date 2021年07月24日 11:04
 **/
public interface F10CashFlowDao {
    /**
     * 分页获取现金流量表
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10CashFlowEntity> pageCashFlow(F10PageReq<F10CommonRequest> requestPageReq);
}
