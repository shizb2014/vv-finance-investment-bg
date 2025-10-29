package com.vv.finance.investment.bg.mongo.dao;

import com.vv.finance.investment.bg.entity.information.StockNewsEntity;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/9/14 17:50
 */
public interface StockNewsDao {

    /**
     * 通过id 查询
     * @param newsId
     * @return
     */
    StockNewsEntity queryByIdAndTopCategory(Long newsId,String topCategory);

    boolean batchSave(List<StockNewsEntity> stockNewsEntityList);

    boolean remove(Long newsId,String topCategory);

    boolean updateByIdAndTopCategory(StockNewsEntity stockNewsEntity);




}
