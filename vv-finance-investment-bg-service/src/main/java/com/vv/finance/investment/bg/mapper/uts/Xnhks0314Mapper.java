package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:53
 * @Version 1.0
 */
@DS("db2")
public interface Xnhks0314Mapper extends BaseMapper<Xnhks0314> {

    @Select(" select null id, SECCODE,#{code} type,concat('并行证券代码：0',F003V) content, concat('并行证券名称：',F005V) content2, F001D releaseDate,  F001D order_date," +
            " REPLACE(LEFT(F009V,10),'/','') date1,REPLACE(RIGHT(F009V,10),'/','') date2,XDBMASK sxdbmask, md5(concat(#{code},x.SECCODE,x.F006D)) uni from vv_uts.xnhks0314 x where x.XDBMASK > #{dbmask} order by x.XDBMASK")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);

    @Select("SELECT SECCODE,f003v,f006d ,f007d,f002v,f005v from XNHKS0314  where f003v in(\n" +
            "SELECT f003v from XNHKS0314 GROUP BY f003v HAVING count(f003v)>1\n" +
            ") order by f003v ,f006d desc")
    List<Xnhks0314> listResultTemp();

    /**
     * 获取指定结束交易日期的临时股票集合
     * @param time
     * @return
     */
    @Select("SELECT SECCODE,f003v,f006d ,f007d ,f002v,f005v from XNHKS0314  where f007d = #{time} and f003v not in(\n" +
            "SELECT f003v from XNHKS0314 where f006d <= #{time} and f007d >= #{time}  GROUP BY f003v HAVING count(f003v)>1\n" +
            ") order by f003v ,f006d desc")
    List<Xnhks0314> findEndTradeTempStock(@Param("time")Long time);

    /**
     * 获取指定交易日期内有交易的临时股票集合
     * @param time
     * @return
     */
    @Select("SELECT SECCODE,f003v,f006d ,f007d ,f002v,f005v,f008v,f009v from XNHKS0314  where f006d <= #{time} and f007d >= #{time}")
    List<Xnhks0314> findTradingTempStockByTime(@Param("time")long time);

    @Select("SELECT SECCODE,f003v,f006d ,f007d ,f002v,f005v,f008v,f009v from XNHKS0314  where F003V = #{f003v} ORDER BY f001d DESC LIMIT 1")
    Xnhks0314 queryOneByF003V(@Param("f003v") String f003v);


    @Select("SELECT f003v,max(f007d) f007d from XNHKS0314  where 1=1 GROUP BY f003v ORDER BY f003v")
    List<Xnhks0314> queryAllTempStocks();
    /**
     * 获取code集合获取临时交易信息集合
     * @return
     */
    @Select("SELECT SECCODE,f003v,f006d ,f007d ,f002v,f005v,f008v,f009v from XNHKS0314  where f003v in ${codes} ")
    List<Xnhks0314> findTempStockInfoByCodes(@Param("codes")String codes);
}
