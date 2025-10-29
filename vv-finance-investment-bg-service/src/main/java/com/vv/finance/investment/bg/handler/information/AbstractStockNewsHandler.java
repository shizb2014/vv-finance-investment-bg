package com.vv.finance.investment.bg.handler.information;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/9/15 10:12
 * 股票新闻处理
 */
@Slf4j
public abstract class AbstractStockNewsHandler {

    @Autowired
    private IStockNewsService stockNewsService;


    /**
     * 全量数据同步
     */
    public abstract void sync();


    void batchSave( List<StockNewsEntity> stockNewsEntities ){
        stockNewsEntities.forEach(this::save
                );
   }
    void save(StockNewsEntity stockNewsEntity){
         stockNewsService.saveOrUpdate(stockNewsEntity,new UpdateWrapper<StockNewsEntity>()
                 .eq("news_id",stockNewsEntity.getNewsId())
                 .eq("top_category",stockNewsEntity.getTopCategory()));
    }

    boolean remove(Long newsId, String topCategory){
       return stockNewsService.remove(new UpdateWrapper<StockNewsEntity>().eq("news_id",newsId).eq("top_category",topCategory));
    }
    boolean updateByNewsIdAndTopCategory(StockNewsEntity stockNewsEntity){
       return stockNewsService.update(stockNewsEntity,new UpdateWrapper<StockNewsEntity>().eq("news_id",stockNewsEntity.getNewsId()).eq("top_category",stockNewsEntity.getTopCategory()));
    }
}
