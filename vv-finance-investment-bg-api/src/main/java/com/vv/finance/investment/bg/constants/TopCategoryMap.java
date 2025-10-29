package com.vv.finance.investment.bg.constants;

import com.vv.finance.investment.bg.entity.information.StockNewsEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hamilton
 * @date 2021/11/10 11:39
 */
public class TopCategoryMap {
    private static final Map<String,String> topCategoryMap=new HashMap<>();
    static {
        topCategoryMap.put("news_24hours","7*24");
        topCategoryMap.put("news_calendar","日历");
        topCategoryMap.put("news_hk_hq","异动");
        topCategoryMap.put("news_hk","港股");
        topCategoryMap.put("news_us","美股");
    }
    public static void setTopCategoryName(List<StockNewsEntity> stockNewsEntityList){
        stockNewsEntityList.forEach(stockNewsEntity -> {
            stockNewsEntity.setTopCategoryName(topCategoryMap.get(stockNewsEntity.getTopCategory()));
        });
    }
}
