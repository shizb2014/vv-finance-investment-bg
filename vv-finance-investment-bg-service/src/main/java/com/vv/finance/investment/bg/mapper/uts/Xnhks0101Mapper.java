package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.domain.filter.EnumValues;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chenyu
 * @since 2021-07-13
 */
@DS("db2")
public interface Xnhks0101Mapper extends BaseMapper<Xnhks0101> {
    /**
     * 获取停牌代码
     * @param date
     * @return
     */
    @Select("SELECT SECCODE FROM `xnhks0101` WHERE F010V = 'S' AND (F009D > ${date} OR F009D is NULL) AND F014V IN('OS','PC','RS')")
    List<String> getCloseCode(@Param("date")String date);

    /**
     * 查询所有的板块和code
     *
     * @return
     */
    @Select("select `CODE` code, `F001V` name from xnhk0003")
    List<EnumValues> listPlates();

    /**
     * 获取港股通代码
     * @return
     */
    @Select("select SECCODE from XNHKS0101 where F022V ='Y' or F032V = 'Y'")
    List<String> getHkStockCodeThrough();

    /**
     * 沽空
     * @return
     */
    @Select("select * from XNHKS0101 where SECCODE = #{code}")
    Xnhks0101 getStockShortSell(@Param("code") String code);

    /**
     * 沽空
     * @return
     */
    @Select("select SECCODE from XNHKS0101 where F006D = F007D and F007D = #{date}")
    List<String> getXnhks0101sToday(@Param("date") Long date);

    /**
     * 获取上市日期
     * @return
     */
    @Select("select F006D from XNHKS0101 where SECCODE = #{code}")
    Long getStockListingDate(@Param("code") String code);

    /**
     * 获取全部股权股票
     * @return
     */
    @Select("select SECCODE as code,F003V as name,F007D as startListingDate,F009D as endListingDate from xnhks0101 where F014V='RS'")
    List<StockRightsDTO> getAllStockRights();

    /**
     * 获取指定日期正在交易的股权股票
     * @return
     */
    @Select("select SECCODE as code,F003V as name,F007D as startListingDate,F009D as endListingDate from xnhks0101 where F014V='RS' and F007D <= #{date} and F009D >= #{date} ")
    List<StockRightsDTO> getTradingStockRights(@Param("date") Long date);

    /**
     * 获取指定日期开始交易的股权股票
     * @return
     */
    @Select("select SECCODE as code,F003V as name,F007D as startListingDate,F009D as endListingDate from xnhks0101 where F014V='RS' and F007D = #{date}  ")
    List<StockRightsDTO> getStartTradingStockRights(@Param("date") Long date);
    /**
     * 获取指定日期结束交易的股权股票
     * @return
     */
    @Select("select SECCODE as code,F003V as name,F007D as startListingDate,F009D as endListingDate from xnhks0101 where F014V='RS' and F009D = #{date}  ")
    List<StockRightsDTO> getEndTradingStockRights(@Param("date") Long date);
}
