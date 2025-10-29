package com.vv.finance.investment.bg.api.index;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.index.TIndexInfo;

/**
 * description: IndexInfoApi
 * date: 2022/6/20 16:45
 * author: fenghua.cai
 */
public interface IndexInfoApi {


    /**
     * 查询指数信息
     *
     * @param code
     * @return
     */
    ResultT<TIndexInfo> queryIndexInfo(String code);
}
