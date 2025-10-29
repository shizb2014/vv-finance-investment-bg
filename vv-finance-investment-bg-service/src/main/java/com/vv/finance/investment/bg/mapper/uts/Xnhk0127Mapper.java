package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.dto.uts.resp.Xnhk0127DTO;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
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
 * @since 2021-02-22
 */
@DS("db2")
public interface Xnhk0127Mapper extends BaseMapper<Xnhk0127> {

    @Select("SELECT t.ID,t.SECCODE,t.F001N,t.F002V,t.F003D,t.F004N,t.F005N,t.F006V,t.F007N,t.Modified_Date,t.Create_Date,t.XDBMASK FROM xnhk0127 t " +
            "WHERE SECCODE IN ( SELECT SECCODE FROM xnhks0112 WHERE F018D = #{queryDate} ) AND F003D IN ( SELECT  DATE_FORMAT(F016D,'%Y-%m-%d')  FROM xnhks0112 WHERE F018D = #{queryDate} )  " +
            "AND F007N = 1  AND F002V IN ( 'CD', 'SD' )  ORDER BY SECCODE DESC")
    List<Xnhk0127> queryDividendStock(@Param("queryDate") Long queryDate);




    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);

    @Select("select t1.* from xnhk0127 t1 join XNHKS0101 t2 on t1.SECCODE = t2.SECCODE and t1.F003D > t2.F007D where t1.F003D <= #{date} and t1.F007N = 1 order by t1.SECCODE desc,F003D asc,F001N asc")
    List<Xnhk0127> getXnhk0127History(@Param("date") Date date);

    @Select("select t2.SECCODE as SECCODE,t2.F003D as F003D,t2.F002V as F002V, t2.F005N as F005N from (\n" +
            "select t.SECCODE,t.F003D,t.F002V,t1.F016D,t1.F003V,t.F005N from xnhk0127 t left join Xnhks0112 t1 \n" +
            "on t.SECCODE = t1.SECCODE and t.F003D = t1.F016D and t1.F003V like CONCAT('%',t.F002V,'%') \n" +
            "where t.F007N = 1 \n" +
            "and (t.F002V not like '%SS%' and t.F002V not like '%SC%')\n" +
            "and (t.Create_Date >= #{startDate} or t.Modified_Date >= #{startDate}) \n" +
            "and (t.Create_Date <= #{endDate} or t.Modified_Date <= #{endDate})\n" +
            " ) t2  where t2.F016D is null or t2.F003V is null \n" +
            "order by t2.SECCODE desc,t2.F003D desc")
    List<Xnhk0127> getXnhk0127NotIncludeSS(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select("select t2.SECCODE as SECCODE,t2.F003D as F003D,t2.F002V as F002V, t2.F005N as F005N from (\n" +
            "select t.SECCODE,t.F003D,t.F002V,t1.F017D,t1.F003V,t.F005N from xnhk0127 t left join Xnhks0112 t1 \n" +
            "on t.SECCODE = t1.SECCODE and t.F003D = t1.F017D and t1.F003V like CONCAT('%',t.F002V,'%') \n" +
            "where t.F007N = 1 \n" +
            "and (t.F002V like '%SS%' or t.F002V like '%SC%')\n" +
            "and (t.Create_Date >= #{startDate} or t.Modified_Date >= #{startDate}) \n" +
            "and (t.Create_Date <= #{endDate} or t.Modified_Date <= #{endDate})\n" +
            " ) t2  where t2.F017D is null or t2.F003V is null \n" +
            "order by t2.SECCODE desc,t2.F003D desc")
    List<Xnhk0127> getXnhk0127IncludeSS(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select("select t.SECCODE,t.F003D,t.F002V,t.F005N from xnhk0127 t where (Create_Date >= #{startDate} or Modified_Date >= #{startDate}) and (Create_Date <= #{endDate} or Modified_Date <= #{endDate}) group by  t.SECCODE,t.F003D,t.F002V,t.F005N having count(1) > 1")
    List<Xnhk0127> getXnhk0127DuplicateData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select("select t.SECCODE,t.F003D,t.F002V,t.F005N from xnhk0127 t where (Create_Date >= #{startDate} or Modified_Date >= #{startDate}) and (Create_Date <= #{endDate} or Modified_Date <= #{endDate})")
    List<Xnhk0127> getXnhk0127sByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    @Select("SELECT DATE_FORMAT(ks.F018D,'%Y-%m-%d') registerDate,k.SECCODE stockCode,k.F003D xrDate,k.F002V xrType,k.F005N changeFactor,k.F006V xrDesc,k.Create_Date,k.F009V " +
            "FROM xnhk0127 k JOIN xnhks0112 ks on k.SECCODE = ks.SECCODE and k.F003D = DATE_FORMAT(ks.F016D,'%Y-%m-%d') " +
            "WHERE k.F007N = 1 AND k.F002V IN ( 'CD', 'SD' ) and k.Create_Date > DATE_FORMAT(ks.F018D,'%Y-%m-%d') " +
            "and k.Create_Date > ADDDATE(NOW(), -#{days}) order by registerDate desc")
    List<Xnhk0127DTO> getMissedDividendRecords(@Param("days") Integer days);

    @Select("SELECT k.SECCODE stockCode,k.F003D xrDate,k.F002V xrType,k.F005N changeFactor,k.F006V xrDesc,k.Create_Date,k.F009V " +
            "FROM xnhk0127 k WHERE k.F007N = 1 AND k.F002V IN ( 'CD', 'SD' ) and k.F003D is null " +
            "and Create_Date > ADDDATE(NOW(), -1) order by xrDate desc")
    List<Xnhk0127DTO> getErrorDividendRecords();
}
