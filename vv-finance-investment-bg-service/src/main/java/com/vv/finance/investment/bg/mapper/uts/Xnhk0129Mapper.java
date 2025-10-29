package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.shareholder.EquityChange;
import com.vv.finance.investment.bg.entity.f10.shareholder.MainShareholding;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolder;
import com.vv.finance.investment.bg.entity.f10.stockMarket.TotalByType;
import com.vv.finance.investment.bg.entity.uts.Xnhk0129;
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
public interface Xnhk0129Mapper extends BaseMapper<Xnhk0129> {

    /**
     * 统计当前时间不同类型的持股占比
     *
     * @param stockCode
     * @return
     */
    // @Select("select x1.F003V type,SUM(x2.F015N) quantity,max(x2.F001D) date from (SELECT SECCODE, max(XDBMASK) XDBMASK, F003V, F005V from xnhks0601 where SECCODE = #{stockCode} GROUP BY F003V, F005V) x1 " +
    //         " left join xnhks0601 x2 on x1.XDBMASK = x2.XDBMASK and x1.F003V = x2.F003V and x1.F005V = x2.F005V WHERE x2.SECCODE = #{stockCode} GROUP BY x1.F003V")
    @Select("select F006V holderType, sum(F042N) as num, sum(F043N) as pop, max(F008D) as date from xnhk0129 where SECCODE = #{stockCode} and F006V is not null and F004V != 'N' group by F006V")
    List<StockHolder> totalQuantityByType(@Param("stockCode") String stockCode);

    @Select("select F003V holderName, F006V holderType, F010V shareType, F042N num, F043N pop, F008D date from xnhk0129 where SECCODE = #{stockCode}")
    List<StockHolder> listStockHoldersByCode(@Param("stockCode") String stockCode);

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
     * 查询主要股东
     *
     * @param page
     * @param stockCode
     * @param holderType
     * @return
     */
    @Select("<script>" +
            "select F003V holderName, F006V holderType, F010V shareType, F042N num, F043N pop, F008D date from xnhk0129 where SECCODE = #{stockCode} and F042N > 0" +
            " <if test=\"holderType != null and holderType != '' \" >" +
            " and F006V = #{holderType}" +
            " </if>" +
            "order by F042N desc" +
            "</script>")
    Page<StockHolder> getMainShareholding(Page<StockHolder> page, @Param("stockCode") String stockCode, @Param("holderType") String holderType);

    /**
     * 获取最新更新时间
     */
    @Select("select F008D from xnhk0129 where SECCODE = #{stockCode} and F006V is not null and F042N > 0 order by F008D desc limit 1")
    Long lastUpdateTime(String stockCode);
}