package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesNonFinancialEntity;

/**
 * @ClassName F10AssetsLiabilitiesDao
 * @Deacription 资产负债表
 * @Author lh.sz
 * @Date 2021年07月24日 11:08
 **/
public interface F10AssetsLiabilitiesDao {

    /**
     * 分页查询金融资产负债
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10AssetsLiabilitiesFinancialEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq);

    /**
     * 分页查询非金融资产负债
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10AssetsLiabilitiesNonFinancialEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq);

    /**
     * 分页查询保险资产负债
     *
     * @param requestPageReq
     * @return
     */
    F10PageResp<F10AssetsLiabilitiesInsuranceEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq);
}
