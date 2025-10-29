package com.vv.finance.investment.bg.mapper.information;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.StockNewsDetailVo;
import com.vv.finance.investment.bg.entity.information.StockNewsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@DS("db2")
public interface NewsHKMapper extends BaseMapper<String> {

    @Select("(SELECT newsid,newstype,newstitle title,date,time,relatesymbol,source,image_url image, content content from news_hk n1 where newsid = #{newsid} limit 1 ) UNION " +
            "(SELECT newsid,newstype,newstitle title,date,time,relatesymbol,source,'' image, content content from news_hk_hq n2 where newsid = #{newsid} limit 1)")
    StockNewsDetailVo findByNewsid(@Param("newsid") String newsid);

    /**
     * 个股资讯列表查询
     * todo sql待完成
     *
     * @param stockNewsVoPage
     * @param stockCode
     * @param indexTime
     * @return
     */
    @Select(" select * from (" +
            " (SELECT newsid,newstype,newstitle,date,time,relatesymbol,source from news_hk where relatesymbol = #{stockCode} n1 ) UNION " +
            " (SELECT newsid,newstype,newstitle,date,time,relatesymbol,source from news_hk_hq where relatesymbol = #{stockCode} and date n2) ) a " +
            " where date > '2020-06-16' ORDER BY date desc,time desc")
    Page<StockNewsVo> pageStockNewsVo(Page<StockNewsVo> stockNewsVoPage, @Param("stockCode") String stockCode, @Param("indexTime") String indexTime);
}
