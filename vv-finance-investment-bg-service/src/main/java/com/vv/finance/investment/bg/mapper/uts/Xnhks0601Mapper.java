package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.shareholder.EquityChange;
import com.vv.finance.investment.bg.entity.f10.shareholder.MainShareholding;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHoldingRiseFall;
import com.vv.finance.investment.bg.entity.f10.stockMarket.TotalByType;
import com.vv.finance.investment.bg.entity.uts.Xnhks0601;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName: Xnhks0601Mapper
 * @Description:
 * @Author: wsliang
 */
@DS("db2")
public interface Xnhks0601Mapper extends BaseMapper<Xnhks0601> {

    /**
     * 统计当前时间不同类型的持股占比
     *
     * @param stockCode
     * @return
     */
    @Select("select x1.F003V type,SUM(x2.F015N) quantity,max(x2.F001D) date from (SELECT SECCODE, max(XDBMASK) XDBMASK, F003V, F005V from xnhks0601 where SECCODE = #{stockCode} GROUP BY F003V, F005V) x1 " +
            " left join xnhks0601 x2 on x1.XDBMASK = x2.XDBMASK and x1.F003V = x2.F003V and x1.F005V = x2.F005V WHERE x2.SECCODE = #{stockCode} GROUP BY x1.F003V")
    List<TotalByType> totalQuantityByType(@Param("stockCode") String stockCode);

    /**
     * 根据季度和类型统计
     *
     * @param stockCode
     * @return
     */
    @Select(" select  x1.F003V type,SUM(x2.F015N) quantity, x1.QUA date from " +
            " (select  SECCODE, max(F001D) F001D, F003V,  F005V, QUA from " +
            " ( SELECT SECCODE, F001D, F003V, F005V,date_format(MAKEDATE(EXTRACT(YEAR  FROM F001D),1) + INTERVAL QUARTER(F001D)*3-1 MONTH,'%Y%m') QUA from xnhks0601 where SECCODE = #{stockCode} ) a" +
            " GROUP BY F003V, F005V, QUA ) x1 left join xnhks0601 x2 on x1.F001D = x2.F001D and x1.F003V = x2.F003V and x1.F005V = x2.F005V WHERE x2.SECCODE = #{stockCode}" +
            " GROUP BY x1.QUA,x1.F003V order by date")
    List<TotalByType> totalQuantityByQuaAndType(@Param("stockCode") String stockCode);

    /**
     * 获取最新的0601
     *
     * @return
     */
    @Select("SELECT a.* FROM xnhks0601 a,( SELECT max( concat( F001D, XDBMASK )) AS concatvalue, SECCODE FROM xnhks0601 GROUP BY SECCODE ) b \n" +
            "WHERE\n" +
            "\ta.SECCODE = b.SECCODE \n" +
            "\tAND concat( a.F001D, a.XDBMASK )= b.concatvalue")
    List<Xnhks0601> getS0601List();

    /**
     * 获取最新的0601-批量code
     *
     * @return
     */
    @Select({
            "<script>",
            "SELECT a.* FROM xnhks0601 a,( SELECT max( concat( F001D, XDBMASK )) AS concatvalue, SECCODE FROM xnhks0601 GROUP BY SECCODE ) b ",
                    "WHERE a.SECCODE = b.SECCODE ",
                    "AND concat( a.F001D, a.XDBMASK )= b.concatvalue AND a.SECCODE in ",
            "<foreach item='id' collection='codes' open='(' separator=',' close=')'>",
            "   #{id}",
            "</foreach>",
            "</script>"
    })
//    @Select("SELECT a.* FROM xnhks0601 a,( SELECT max( concat( F001D, XDBMASK )) AS concatvalue, SECCODE FROM xnhks0601 GROUP BY SECCODE ) b \n" +
//            "WHERE\n" +
//            "\ta.SECCODE = b.SECCODE \n" +
//            "\tAND concat( a.F001D, a.XDBMASK )= b.concatvalue AND a.SECCODE in " +
//            "            <foreach collection=\"codes\" item=\"item\" separator=\",\">\n" +
//            "                #{item}\n" +
//            "            </foreach>\n" +
//            "            " +
//            "")
    List<Xnhks0601> getS0601ListByCodes(@Param("codes")List<String> codes);

    /**
     * 查询主要股东
     *
     * @param page
     * @param stockCode
     * @return
     */
    @Select(" select x1.F001D,x1.F042V type,x1.F005V shareholdingName,x1.F015N num,round(x1.F014N,2) ratio, round(x1.F016N - IFNULL(x1.F014N, 0), 2) ratioChange " +
            " from xnhks0601 x1 where x1.SECCODE = #{stockCode} and CONCAT(x1.F001D,x1.XDBMASK) in (select max(con) from (select F005V, F001D, CONCAT(F001D, max(XDBMASK)) con " +
            " from xnhks0601 where SECCODE = #{stockCode} group by F001D, F005V) a group by F005V) order by ratio desc,shareholdingName")
    Page<MainShareholding> getMainShareholding(Page<MainShareholding> page, @Param("stockCode") String stockCode);

    /**
     * 获取最新更新时间
     */
    @Select("select F001D from xnhks0601 where SECCODE = #{stockCode} order by F001D desc limit 1")
    Long lastUpdateTime(String stockCode);

    @Select(" select releaseDate, shareholdingName, positionType, positionChange, averagePrice, currencyType, num, ratio " +
            " from ((select F002D releaseDate, F005V shareholdingName, IF(F015N < F013N,0 - F007N,F007N) positionChange, round(F010N,3) averagePrice, F008V currencyType, F015N num, round(F014N,2) ratio, '好仓' positionType " +
            " from xnhks0601 where SECCODE = #{stockCode} and F006V > 0 and F007N > 0 ) " +
            " Union all (select F002D releaseDate, F005V shareholdingName, IF(F026N >= F024N,F018N,0 - F018N) positionChange, round(F021N,3) averagePrice, F008V currencyType, F026N num,round(F027N,2) ratio, '淡仓' positionType " +
            " from xnhks0601 where SECCODE = #{stockCode} and F017V > 0 and F018N > 0 )) xx order by releaseDate desc")
    Page<EquityChange> listEquityChanges(Page<EquityChange> page, @Param("stockCode") String stockCode);

    @Select("<script>" +
            "select\n" +
            "       releaseDate,shareholdingName,positionType,positionKey,positionChange,averagePrice,currencyType,num,ratio\n" +
            "from ((select F002D                               releaseDate,\n" +
            "              F005V                               shareholdingName,\n" +
            "              IF(F015N &lt; F013N, 0 - F007N, F007N) positionChange,\n" +
            "              round(F010N, 3)                     averagePrice,\n" +
            "              F008V                               currencyType,\n" +
            "              F015N                               num,\n" +
            "              round(F016N, 2)                     ratio,\n" +
            "              '好仓'                               positionType,\n" +
            "              'rise'                               positionKey,\n" +
            "              xdbmask\n" +
            "       from xnhks0601\n" +
            "       where SECCODE = #{stockCode} and F006V &gt; 0 and F007N &gt; 0)\n" +
            "       Union all\n" +
            "       (select F002D                               releaseDate,\n" +
            "              F005V                                shareholdingName,\n" +
            "              IF(F026N &gt;= F024N, F018N, 0 - F018N) positionChange, \n" +
            "              round(F021N, 3)                      averagePrice,\n" +
            "              F008V                                currencyType,\n" +
            "              F026N                                num,\n" +
            "              round(F027N, 2)                      ratio,\n" +
            "              '淡仓'                                positionType,\n" +
            "              'fall'                                positionKey,\n" +
            "              xdbmask\n" +
            "       from xnhks0601\n" +
            "       where SECCODE = #{stockCode} and F017V &gt; 0 and F018N &gt; 0)) xx\n" +
            "where 1 = 1 " +
            " <if test=\"positionKey != null and positionKey != '' \" >" +
            " and xx.positionKey = #{positionKey}" +
            " </if>" +
            " order by xx.releaseDate desc, xx.xdbmask desc" +
            "</script>")
    Page<EquityChange> listEquityChangesV2(Page<EquityChange> page, @Param("stockCode") String stockCode, @Param("positionKey") String positionKey);


    @Select("SELECT\n" +
            "\ta.SECCODE,\n" +
            "\ta.f005v,\n" +
            "\ta.F007N,\n" +
            "\ta.F018N,\n" +
            "\ta.F010N,\n" +
            "\ta.F021N,\n" +
            "\ta.F014N,\n" +
            "\ta.F027N,\n" +
            "\ta.F002D \n" +
            "FROM\n" +
            "\tXNHKS0601 a\n" +
            "\tRIGHT JOIN ( SELECT SECCODE, MAX( XDBMASK ) XDBMASK FROM XNHKS0601 GROUP BY SECCODE ) b ON a.SECCODE = b.SECCODE \n" +
            "\tAND a.XDBMASK = b.XDBMASK \n" +
            "ORDER BY\n" +
            "\ta.F002D DESC")
    Page<Xnhks0601> pageByLast(Page<Xnhks0601> page);

    @Select("select\n" +
            "    QUA date, sum(positionChange * IFNULL(averagePrice, 0)) changeAmount, sum(IF(IDH = 'rise', positionChange * averagePrice, 0)) riseAmount, " +
            "    sum(IF(IDH = 'fall', positionChange * averagePrice, 0)) fallAmount\n" +
            "from ((select\n" +
            "           IF(F015N < F013N, 0 - F007N, F007N) positionChange,\n" +
            "           round(F010N, 3)                     averagePrice,\n" +
            "           date_format(MAKEDATE(EXTRACT(YEAR FROM F002D), 1) + INTERVAL QUARTER(F002D) * 3 - 1 MONTH, '%Y%m') as QUA,\n" +
            "           IF(IF(F015N < F013N, 0 - F007N, F007N) > 0 = true, 'rise', 'fall') as IDH\n" +
            "       from xnhks0601\n" +
            "       where SECCODE = #{stockCode} and F006V > 0 and F007N > 0 and F002D >= #{startDate} and F002D <= #{endDate})\n" +
            "      Union all(\n" +
            "          select\n" +
            "              IF(F026N >= F024N, F018N, 0 - F018N) positionChange,\n" +
            "              round(F021N, 3)                      averagePrice,\n" +
            "              date_format(MAKEDATE(EXTRACT(YEAR FROM F002D), 1) + INTERVAL QUARTER(F002D) * 3 - 1 MONTH, '%Y%m') as QUA,\n" +
            "              IF(IF(F015N < F013N, 0 - F007N, F007N) > 0 = true, 'rise', 'fall') as IDH\n" +
            "          from xnhks0601\n" +
            "          where SECCODE = #{stockCode} and F017V > 0 and F018N > 0 and F002D >= #{startDate} and F002D <= #{endDate})) xx\n" +
            "group by QUA\n" +
            "order by QUA desc;")
    List<StockHoldingRiseFall> totalStockHolderChangeAmount(@Param("stockCode") String stockCode, @Param("startDate") long startDate, @Param("endDate") long endDate);
}