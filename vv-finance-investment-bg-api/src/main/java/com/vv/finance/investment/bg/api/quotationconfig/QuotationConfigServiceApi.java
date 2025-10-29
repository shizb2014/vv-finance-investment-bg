package com.vv.finance.investment.bg.api.quotationconfig;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.quotationconfig.CustomQuotationConfigDTO;
import com.vv.finance.investment.bg.dto.quotationconfig.UserQuotationConfigResDTO;

import java.util.List;

/**
 * description: QuotationConfigServiceApi
 * date: 2022/8/11 10:08
 * author: fenghua.cai
 */
public interface QuotationConfigServiceApi {

    /**
     * 添加用户行情自定义周期
     * @param customQuotationConfigDTO
     * @return
     */
    ResultT<Long> addUserCustomQuotationConfig(CustomQuotationConfigDTO customQuotationConfigDTO, String userName);

    /**
     * 删除用户行情自定义周期
     * @param id
     * @return
     */
    ResultT<Integer> removeUserCustomQuotationConfig(Long id);

    /**
     * 获取用户行情自定义周期
     * @return
     */
    ResultT<List<UserQuotationConfigResDTO>> getUserCustomQuotationConfig(String userName);

    /**
     * 获取用户行情自定义周期(新)
     * @return
     */
    ResultT<List<UserQuotationConfigResDTO>> getUserCustomQuotationConfigNew(Integer regionType,String userName);
}
