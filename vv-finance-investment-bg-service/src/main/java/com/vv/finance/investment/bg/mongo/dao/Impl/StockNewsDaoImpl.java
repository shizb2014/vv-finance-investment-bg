package com.vv.finance.investment.bg.mongo.dao.Impl;

import com.mongodb.client.result.DeleteResult;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.mongo.dao.StockNewsDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/9/14 17:54
 */
@Component
@RequiredArgsConstructor
public class StockNewsDaoImpl implements StockNewsDao {
    private final MongoTemplate mongoTemplate;


    @Override
    public StockNewsEntity queryByIdAndTopCategory(Long newsId, String topCategory) {
        return mongoTemplate.findOne(buildIdAndTopCategory(newsId,topCategory),StockNewsEntity.class);
    }

    @Override
    public boolean batchSave(List<StockNewsEntity> stockNewsEntityList) {
        Collection<StockNewsEntity> stockNewsEntities = mongoTemplate.insertAll(stockNewsEntityList);
        return stockNewsEntities.size()==stockNewsEntityList.size();
    }

    @Override
    public boolean remove(Long newsId, String topCategory) {
        DeleteResult remove = mongoTemplate.remove(buildIdAndTopCategory(newsId, topCategory), StockNewsEntity.class);
        return remove.wasAcknowledged();

    }

    @Override
    public boolean updateByIdAndTopCategory(StockNewsEntity stockNewsEntity) {
        remove(stockNewsEntity.getNewsId(),stockNewsEntity.getTopCategory());
        mongoTemplate.save(stockNewsEntity);
        return true;
    }

    private Query buildIdAndTopCategory(Long newsId, String topCategory){
        return Query.query(Criteria.where("topCategory").is(topCategory).and("newsId").is(newsId));
    }
}
