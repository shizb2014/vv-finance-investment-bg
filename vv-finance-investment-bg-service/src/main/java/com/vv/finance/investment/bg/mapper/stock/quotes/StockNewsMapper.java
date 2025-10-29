package com.vv.finance.investment.bg.mapper.stock.quotes;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.common.entity.quotation.f10.ComNewShareCalendarVO;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.information.StockNewsVo;
import com.vv.finance.investment.bg.entity.information.TagDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2021-09-15
 */
public interface StockNewsMapper extends BaseMapper<StockNewsEntity> {

    /**
     * 查询历史数据
     *
     * @param stockNewsVoPage
     * @param id
     * @param stockCode
     * @return
     */
    @Select(" select id, news_id newsid, second_category newsType, news_title title, CONCAT(UNIX_TIMESTAMP(date_time),'000') dateTime, relation_stock stockCode,market,image_url image, source , xdbmask" +
            " from t_stock_news where (#{id} = 0 or id <= #{id}) and publish_status = 0 " +
            " and top_category in ('news_hk','news_hk_hq') and FIND_IN_SET(#{stockCode}, relation_stock)" +
            " order by date_time desc, id desc")
    Page<StockNewsVo> pageOldStockNewsVoInHkAndHq(Page<StockNewsVo> stockNewsVoPage, @Param("id") Long id,
                                                  @Param("stockCode") String stockCode);

    @Select(" select id, news_id newsid, second_category newsType, news_title title, CONCAT(UNIX_TIMESTAMP(date_time),'000') dateTime, relation_stock stockCode,market,image_url image, source , xdbmask" +
            " from t_stock_news where (#{id} = 0 or id <= #{id}) and publish_status = 0 " +
            " and top_category in ('news_hk','news_hk_hq') and (#{stockCodes} = '' or concat(',',relation_stock, ',') REGEXP concat(',',#{stockCodes},',')) " +
            " order by date_time desc, id desc")
    Page<StockNewsVo> pageOldStockNewsVoInHkAndHq2(Page<StockNewsVo> stockNewsVoPage, @Param("id") Long id,
                                                   @Param("stockCodes") String stockCode);

    @Select(" select id, news_id newsid, second_category newsType, news_title title, CONCAT(UNIX_TIMESTAMP(date_time),'000') dateTime, relation_stock stockCode,market,image_url image, source , xdbmask" +
            " from t_stock_news FORCE INDEX(index_status_top_stock) where (#{id} = 0 or id < #{id}) and publish_status = 0 and date_time <= #{time}" +
            " and top_category in ${topCategorys} and (#{stockCodes} = '' or concat(',',relation_stock, ',') REGEXP concat(',',#{stockCodes},',')) " +
            " order by date_time desc, id desc limit #{num}")
    List<StockNewsVo> pageOldStockNewsVoInHkAndHq3(@Param("id") Long id, @Param("stockCodes") String stockCode, @Param("time") LocalDateTime time, @Param("num") Long num,@Param("topCategorys")String topCategorys);


    /**
     * 查询历史自选股相关资讯
     *
     * @param stockNewsEntityPage
     * @param stockCode           多个关联股票代码，英文逗号间隔
     * @return
     */
    @Select(" select n.* from t_stock_news n where n.publish_status = 0 and n.top_category in ('news_hk','news_hk_hq') and (#{stockCodes} = '' or concat(',',n.relation_stock, ',') REGEXP concat(',',#{stockCodes},','))" +
            " and (#{id} = 0 or id <= #{id}) order by date_time desc, id desc")
    Page<StockNewsEntity> pageOldByStocksInHkAndHq(Page<StockNewsEntity> stockNewsEntityPage, @Param("stockCodes") String stockCode,
                                                   @Param("id") Long id);

    /**
     * 查询历史自选股相关资讯
     *
     * @param stockCode           多个关联股票代码，英文逗号间隔
     * @param time
     * @param num
     * @return
     */
    @Select(" select n.* from t_stock_news n where n.publish_status = 0 and n.top_category in ('news_hk','news_hk_hq') and (#{stockCodes} = '' or concat(',',n.relation_stock, ',') REGEXP concat(',',#{stockCodes},','))" +
            " and (#{id} = 0 or id < #{id}) and date_time <= #{time} order by date_time desc, id desc limit #{num}")
    List<StockNewsEntity> pageOldByStocksInHkAndHqV2(@Param("stockCodes") String stockCode, @Param("id") Long id, @Param("time") LocalDateTime time, @Param("num") Long num);
    /**
     * 查询历史自选股相关资讯
     *
     * @param stockCode           多个关联股票代码，英文逗号间隔
     * @param time
     * @param num
     * @return
     */
    @Select(" select n.* from t_stock_news n where n.publish_status = 0 and n.top_category in ('news_hk','news_hk_hq') and (#{stockCodes} = '' or concat(',',n.relation_stock, ',') REGEXP concat(',',#{stockCodes},','))" +
            " and (#{id} = 0 or id < #{id}) and date_time <= #{time} order by date_time desc, id desc limit #{num}")
    List<StockNewsEntity> pageOldByStocksInHkAndHqV3(@Param("stockCodes") String stockCode, @Param("id") Long id, @Param("time") LocalDateTime time, @Param("num") Long num);

    /**
     * 查询最新自选股相关资讯
     *
     * @param stockNewsEntityPage
     * @param stockCode           多个关联股票代码，英文逗号间隔
     * @return
     */
    @Select(" select n.* from t_stock_news n where n.publish_status = 0 and n.top_category in ('news_hk','news_hk_hq') and (#{stockCodes} = '' or concat(',',n.relation_stock, ',') REGEXP concat(',',#{stockCodes},',')) " +
            " and id > #{id} order by date_time desc, id desc")
    Page<StockNewsEntity> pageByStocksInHkAndHq(Page<StockNewsEntity> stockNewsEntityPage, @Param("stockCodes") String stockCode,
                                                @Param("id") Long id);

    /**
     * 根据地区查询国家列表
     *
     * @param areaCode
     * @return
     */
    @Select({
            "<script>",
            "SELECT concat(country,'%') FROM t_stock_news_area where areaCode in",
            "<foreach collection='areaCodes' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<String> listCountryName(@Param("areaCodes") List<String> areaCode);

    /**
     * 查询所有标签的code和value
     * @return
     */
    @Select("select code, tagValue, category from t_stock_news_tag")
    List<TagDto> listAllTags();


    /**
     * 查询资讯id和对应地区
     * @param ids
     * @return
     */
    @Select("select distinct CONCAT(n.id,',',a.areaName) from t_stock_news n left JOIN t_stock_news_area a on n.content like CONCAT(a.country,'%') where n.id in ${ids}")
    List<String> queryAreaByIds(@Param("ids") String ids);

    @Select({
            "<script>" +
            "select 2 as calendarEventCode, REPLACE(date,'-','') as date,count(1) as calendarEventNum from t_stock_news " +
                    "where date >= #{startDate} " +
                    "and publish_status = 0 " +
                    "and  top_category = 'news_calendar' " +
                    "and second_category = '经济事件' " +
                    "<if test=\"endDate != null and endDate != ''\">\n" +
                    "            and date &lt;= #{endDate}\n" +
                    "        </if> " +
                    "group by date" +
            "</script>"
    })
    List<ComNewShareCalendarVO> queryStockNewsByType(@Param("startDate")String startDate, @Param("endDate")String endDate);
    @Select("select id,relation_stock from t_stock_news where relation_stock is not null")
    List<StockNewsEntity> findRelationCode();

    @Select(" select * from (select * from t_stock_news where (#{id} = 0 or id < #{id}) and publish_status = 0 " +
            " and top_category in ${topCategory} and second_category in ${tags} and (#{dateTime} is null or date_time <= #{dateTime})" +
            " order by id desc limit #{maxNum}) t order by t.date_time desc, t.id desc limit #{num}")
    List<StockNewsEntity> queryHKAndHQNews(@Param("topCategory") String topCategory, @Param("tags") String tags, @Param("id") Long id, @Param("dateTime") LocalDateTime dateTime, @Param("maxNum") Long maxNum, @Param("num") Long num);
}
