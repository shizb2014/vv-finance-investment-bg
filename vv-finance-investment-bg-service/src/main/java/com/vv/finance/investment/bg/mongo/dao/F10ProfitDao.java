package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;

import java.util.List;

/**
 * @ClassName F10ProfitDao
 * @Deacription 利润表
 * @Author lh.sz
 * @Date 2021年07月24日 11:05
 **/
public interface F10ProfitDao {

    /**
     * 查询每只股票每种类型报表的最新数据
     *
     * @return
     */
    <T> List<T> listProfitEachCodeAndType(Class<T> clazz, String collectionName);

    /**
     * 分页查询金融利润表
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10FinProfitEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq);

    /**
     * 分页查询非金融利润表
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10NoFinProfitEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq);

    /**
     * 分页查询保险利润表
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10InsureProfitEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq);
}
