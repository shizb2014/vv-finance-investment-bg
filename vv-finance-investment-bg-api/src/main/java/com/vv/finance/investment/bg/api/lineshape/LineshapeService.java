package com.vv.finance.investment.bg.api.lineshape;

/**
 * @Auther: GMC
 * @Date: 2024/6/28 10:53
 * @Description: com.vv.finance.investment.bg.api.lineshape
 * @version: 1.0
 */
public interface LineshapeService {
    /**
     * 删除临时股票形态数据
     * @param stockCode
     * @return
     */
    void delLineshapeByStockCode(String stockCode);
    /**
     * 变更形态股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void upLineshapeStockCode(String sourceCode, String targetCode);
    /**
     * 新增模拟股票形态数据
     *
     * @param simulateCode 模拟股票code
     */
    void saveSimulateLineshapeInfo(String simulateCode);

}
