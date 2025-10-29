package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.ExitRightVo;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author chenyu
 * @since 2021-07-13
 */
@DS("db2")
@Repository
public interface Xnhks0112Mapper extends BaseMapper<Xnhks0112> {

    /**
     * 历史翻页除权资讯
     * @param startTime
     * @param endTime
     * @param stocks
     * @return
     */
    @Select(" select x1.* from (SELECT SECCODE, F006V dividend, F003V dividendType, xdbmask, " +
            " UNIX_TIMESTAMP(IFNULL(F010D,IFNULL(F011D,IFNULL(F012D,IFNULL(F013D,IFNULL(F014D,IFNULL(F015D,IFNULL(F017D,'--')))))))) dividendDate " +
            " from xnhks0112 where F006V != '无派息' " +
            " and ('' in ${stocks} or SECCODE in ${stocks} )" +
            " ) x1 " +
            " where (#{startTime} is null or x1.dividendDate >= #{startTime} ) and (#{endTime} is null or x1.dividendDate <= #{endTime} )" +
            " ORDER BY x1.dividendDate asc,x1.xdbmask asc"
    )
    Page<ExitRightVo> pageOldExitRight(Page<ExitRightVo> page, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("stocks") String stocks);

    @Select("select t2.SECCODE,t2.F017D,t2.F003V from (\n" +
            "select t.SECCODE,t1.F003D,t1.F002V,t.F017D,t.F003V from Xnhks0112 t left join xnhk0127 t1 \n" +
            "on t.SECCODE = t1.SECCODE and t1.F003D = t.F017D and t.F003V like CONCAT('%',t1.F002V,'%')  and t1.F007N = 1 \n" +
            "where\n" +
            "(F003V like '%SS%' or F003V like '%SC%') \n" +
            "and (t.Create_Date >= #{startDate} or t.Modified_Date >= #{startDate}) \n" +
            "and (t.Create_Date <= #{endDate} or t.Modified_Date <= #{endDate}) \n" +
            " ) t2 where t2.F003D is null or t2.F002V is null order by t2.SECCODE desc,t2.F017D desc")
    List<Xnhks0112> getXnhk0112sIncludeSS(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select("select t2.SECCODE,t2.F016D,t2.F003V from (\n" +
            "select t.SECCODE,t1.F003D,t1.F002V,t.F016D,t.F003V from Xnhks0112 t left join xnhk0127 t1 \n" +
            "on t.SECCODE = t1.SECCODE and t1.F003D = t.F016D and t.F003V like CONCAT('%',t1.F002V,'%')  and t1.F007N = 1 \n" +
            "where\n" +
            "(F003V not like '%SS%' and F003V not like '%SC%') \n" +
            "and (t.Create_Date >= #{startDate} or t.Modified_Date >= #{startDate}) \n" +
            "and (t.Create_Date <= #{endDate} or t.Modified_Date <= #{endDate}) \n" +
            " ) t2 where t2.F003D is null or t2.F002V is null order by t2.SECCODE desc,t2.F016D desc")
    List<Xnhks0112> getXnhk0112sNotIncludeSS(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
