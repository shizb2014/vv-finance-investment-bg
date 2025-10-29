package com.vv.finance.investment.bg.mapper.stock.quotes;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.entity.quotation.f10.ComNewShareCalendarVO;
import com.vv.finance.investment.bg.dto.stock.TypeSxdbmask;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wsliang
 * @date 2021/9/22 15:39
 **/
public interface CompanyTrandsMergeMapper extends BaseMapper<CompanyTrendsMergeEntity> {
    /**
     * 合并交易警报
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content`,`content2`,`releaseDate` ,`date1` ,`date2`, sxdbmask, uni) " +
            " (select x1.SECCODE,#{code} type,x2.F001V content,x3.F001V content2, x1.F001D releaseDate,x1.F001D date1,null,x1.XDBMASK, md5(concat(#{code},x1.SECCODE,x1.F001D,x1.F003V)) uni " +
            " from vv_uts.xnhk0311 x1 left join vv_uts.xnhk0007 x2 on x1.F002V = x2.CODE left join vv_uts.xnhk0006 x3 on x1.F002V = x3.CODE " +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x1.XDBMASK and c.type = #{code})) ON DUPLICATE KEY UPDATE " +
            " type = type")
    Integer mergeAlarm(Integer code);

    /**
     * 合并并行交易
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`content2`,`releaseDate` ,`date1` ,`date2`, sxdbmask,uni)" +
            " (select SECCODE,#{code} type,concat('并行证券代码：0',F003V) content, concat('并行证券名称：',F005V) content2, F001D releaseDate,REPLACE(LEFT(F009V,10),'/','') date1,REPLACE(RIGHT(F009V,10),'/','') date2,XDBMASK, md5(concat(#{code},x.SECCODE,x.F006D)) uni   " +
            " from vv_uts.xnhks0314 x where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE " +
            " type = type")
    Integer mergeParallel(Integer code);

    /**
     * 合并停复牌
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask, uni)" +
            " (select x1.SECCODE,#{code} type,'' content, x1.F001D releaseDate,x1.F002D,x2.F002D,x1.XDBMASK, md5(concat(#{code},x1.SECCODE,x1.F002D)) uni from " +
            " vv_uts.XNHKS0317 x1 left join vv_uts.XNHK0318 x2 on x1.SECCODE = x2.SECCODE and x1.F002D = x2.F003D " +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x1.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE type = type")
    Integer mergeTrading(Integer code);

    /**
     * 合并股东大会
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask, uni)" +
            " (select x.SECCODE,#{code} type,x2.F001V content, x.F001D releaseDate,x.F003D,null,x.XDBMASK, md5(concat(#{code},x.SECCODE,x.F002V)) uni from vv_uts.XNHKS0310 x" +
            " left join vv_uts.xnhk0009 x2 on x.F005V = x2.CODE " +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE type = type")
    Integer mergeMetting(Integer code);

    /**
     * 合并公司重组
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask, uni)" +
            " (select SECCODE,#{code} type,F004V content, F001D releaseDate,F003D date1,null,XDBMASK, md5(concat(#{code},x.SECCODE,x.F002D)) uni  from vv_uts.XNHKS0309 x" +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE type = type")
    Integer mergeReorganize(Integer code);

    /**
     * 合并收购及合并
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask, uni)" +
            " (select SECCODE,#{code} type,F006V content, F001D releaseDate,F005D date1,null,XDBMASK, md5(concat(#{code},x.SECCODE,x.F001D)) uni  from vv_uts.XNHKS0308 x" +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE type = type")
    Integer mergePurchase(Integer code);

    /**
     * 分红派息
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask)" +
            " (select SECCODE,#{code} type,F006V content, F001D releaseDate,F001D,null,XDBMASK  from vv_uts.XNHKS0112 x" +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) ) ON DUPLICATE KEY UPDATE type = type")
    Integer mergeDividend(Integer code);

    /**
     * 股票回购
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask)" +
            " (select SECCODE,#{code} type,F009N content, F001D releaseDate,F001D,null,XDBMASK  from vv_uts.XNHK0602 x" +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) )")
    Integer mergeRepurchase(Integer code);

    /**
     * 拆股合并
     *
     * @param code
     * @return
     */
    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content` ,`releaseDate` ,`date1` ,`date2`, sxdbmask)" +
            " (select SECCODE,#{code} type,F003V content, F001D releaseDate,F002D,null,XDBMASK  from vv_uts.XNHKS0320 x" +
            " where not exists (select c.sxdbmask from t_company_trends_merge c where c.sxdbmask=x.XDBMASK and c.type = #{code}) )")
    Integer mergeSplitMerger(Integer code);

    @Select(" select type, max(sxdbmask) sxdbmask from t_company_trends_merge GROUP BY type order by type")
    List<TypeSxdbmask> findMaxDbmaskGroupType();

    @Insert(" insert into t_company_trends_merge (`SECCODE`,`type`,`content`,`content2` ,`releaseDate`,`order_date` ,`date1` ,`date2`, `sxdbmask`,`uni`) " +
            " values (#{SECCODE},#{type},#{content},#{content2},#{releaseDate},#{orderDate},#{date1},#{date2},#{sxdbmask},#{uni}) ON DUPLICATE KEY UPDATE sxdbmask = #{sxdbmask}")
    void save(CompanyTrendsMergeEntity entity);

    /**
     * 保存，如果触发唯一索引，则更新
     * @param entity
     */
    void batchSaveDupUpdate(List<CompanyTrendsMergeEntity> entity);


    /**
     * 保存，如果触发唯一索引，则更新
     * @param entity
     */
    void saveDupUpdate(CompanyTrendsMergeEntity entity);

    /**
     * 通过 uni 唯一索引删除一条记录
     *
     * @param uni
     */
    void removeByUni(@Param("type") Integer code, @Param("uni") String uni);

    List<CompanyTrendsMergeEntity> queryCompanyTrendsNum(@Param("startDate")String startDate, @Param("endDate")String endDate);

}
