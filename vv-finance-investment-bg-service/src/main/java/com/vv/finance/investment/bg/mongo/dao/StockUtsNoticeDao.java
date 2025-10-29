//package com.vv.finance.investment.bg.mongo.dao;
//
//import com.vv.finance.investment.bg.dto.uts.resp.NoticeMongoPageResp;
//import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
//
//import java.util.List;
//
///**
// * @author chenyu
// * @date 2021/7/19 16:37
// */
//public interface StockUtsNoticeDao {
//    /**
//     * 插入
//     * @param stockUtsNoticeEntity
//     */
//    Integer saveOrUpdate(StockUtsNoticeEntity stockUtsNoticeEntity);
//
//    /**
//     * 查询公告
//     * @param types
//     * @param type
//     * @param code
//     * @param pageSize
//     * @param currentPage
//     * @return
//     */
//    NoticeMongoPageResp queryNotice(List<String> types, Integer type, String code, Integer pageSize, Integer currentPage);
//}
