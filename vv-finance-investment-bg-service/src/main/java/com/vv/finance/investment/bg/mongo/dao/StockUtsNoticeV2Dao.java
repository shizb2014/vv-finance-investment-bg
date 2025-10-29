package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.investment.bg.dto.uts.resp.NoticeMongoPageResp;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntityV2;

import java.util.List;

/**
 * @author chenyu
 * @date 2021/7/19 16:37
 */
public interface StockUtsNoticeV2Dao {
    /**
     * 插入
     * @param stockUtsNoticeEntity
     */
    Integer saveOrUpdate(StockUtsNoticeEntityV2 stockUtsNoticeEntity,List<Integer> flags);

    /**
     * 查询公告
     * @param types
     * @param type
     * @param code
     * @param pageSize
     * @param currentPage
     * @return
     */
    NoticeMongoPageResp queryNotice(List<String> types, Integer type, String code, Integer pageSize, Integer currentPage);

    /**
     * 通过目录获取目录下的所有文件
     * @param dirs 目录
     * @return
     */
    List<StockUtsNoticeEntityV2> getNoticByDirs(String dirs);
    /**
     * 删除临时股票资讯
     *
     * @param stockCode
     */
    void delByStockCode(String stockCode);
    /**
     * 变更公告股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    void upNoticeStockCode(String sourceCode, String targetCode);
    /**
     * 新增模拟股票公告数据
     *
     * @param simulateCode 模拟股票code
     */
    void saveSimulateNoticeInfo(String simulateCode);
}
