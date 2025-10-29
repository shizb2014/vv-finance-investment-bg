package com.vv.finance.investment.bg.stock.f10.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolderChange;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 股票码表 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface StockHolderChangeMapper extends BaseMapper<StockHolderChange> {

    @Select("SELECT t.* FROM (SELECT holder_type, max(change_date) as change_date FROM t_stock_holder_change GROUP BY holder_type) a LEFT JOIN t_stock_holder_change t ON t.holder_type = a.holder_type and t.change_date = a.change_date where t.code = #{stockCode}")
    List<StockHolderChange> getLatestHolderChangeByCode(@Param("stockCode") String stockCode);

    @Select("select * from vv_finance_stock.t_stock_holder_change where code = #{stockCode} and qua_date = #{quaDate}")
    List<StockHolderChange> getHolderChangeList(@Param("stockCode") String stockCode, @Param("quaDate") Long quaDate);

    @Select(
            " SELECT code, holder_type, share_type, num, pop, qua_date FROM ( " +
            "      SELECT *, ROW_NUMBER() OVER (PARTITION BY code, holder_type, qua_date ORDER BY change_date desc ) AS row_num " +
            "      FROM t_stock_holder_change where code = #{stockCode} " +
            "      ORDER BY code, qua_date, holder_type" +
            "  ) AS temp_table " +
            " WHERE row_num = 1"
    )
    List<StockHolderChange> listHolderChangesByCode(@Param("stockCode") String stockCode);
}
