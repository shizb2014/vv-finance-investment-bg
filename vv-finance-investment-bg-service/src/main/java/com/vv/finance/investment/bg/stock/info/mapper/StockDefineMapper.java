package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.common.entity.quotation.StockDefinePageReq;
import com.vv.finance.investment.bg.entity.f10.SubBusinessInfo;
import com.vv.finance.investment.bg.stock.info.StockDefine;
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
public interface StockDefineMapper extends BaseMapper<StockDefine> {
    /**
     * 查询股票或者指数
     *
     * @param key
     * @param num
     * @param list
     * @return
     */
    List<StockDefine> queryStock(@Param("key") String key, @Param("num") int num, @Param("list") List<String> list);


    @Select(" select de.industry_code busCode, su.name busName from t_stock_define de left join t_stock_define su " +
            " on de.industry_code = su.code where de.code = #{stockCode}")
    SubBusinessInfo queryStockWithIndustry(@Param("stockCode") String stockCode);

    List<String> selectStockCodeList();

    Integer updateStockDefineById(StockDefine stockDefine);

    int batchSaveOrUpdate(List<StockDefine> stockDefine);

    List<Long> selectNoCompStocks();

    Page<StockDefine> pageStockDefine(Page<StockDefine> page, @Param("req") StockDefinePageReq req);
}
