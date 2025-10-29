package com.vv.finance.investment.bg.api.information;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.information.app.FreeStockNewsVoApp;
import com.vv.finance.investment.bg.entity.information.app.StockNewsDetailVoApp;

import java.util.List;

/**
 * @ClassName InformationAppApi
 * @Deacription 资讯api - app
 * @Author lh.sz
 * @Date 2021年09月15日 17:34
 **/
public interface InformationAppApi {
    /**
     * 分页获取异动资讯 v2
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> transactionInformationPageV2(CommonNewsPage page);

    /**
     * 分页获取新股资讯 v2
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> newShareInformationPageV2(CommonNewsPage page);

    /**
     * 分页获取港美股资讯 v2
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> hkOrAmericanInformationPageV2(CommonNewsPage page, CommonNewsPage.QueryCodeEnum type);

    /**
     * 分页查询个股数据 v2
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> listNewsBySimpleStockVoV2(CommonNewsPage pageReq);

    /**
     * 分页查询自选股资讯 v2
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> listFreeNewsV2(CommonNewsPage pageReq, List<String> stockCodes);

    /**
     * 权证资讯列表  V2
     * @param pageReq
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVoApp>> listNewsByWarrantStockVoV2(CommonNewsPage pageReq);

    /**
     * 资讯详情 v2
     *
     * @param newsid
     * @return
     */
    ResultT<StockNewsDetailVoApp> findFreeDetailByIdV2(Long newsid, boolean needWarrant);
    /**
     * 删除临时股票资讯
     *
     * @param stockCode
     */
    void delByStockCode(String stockCode);
    /**
     * 变更资讯股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void upInformationStockCode(String sourceCode,String targetCode);
    /**
     * 新增模拟股票资讯数据
     *
     * @param simulateCode 模拟股票code
     */
    void saveSimulateInformation(String simulateCode);

    ResultT<PageWithTime<FreeStockNewsVoApp>> assignInformationPageV2(CommonNewsPage page, List<String> stockCodes);
}
