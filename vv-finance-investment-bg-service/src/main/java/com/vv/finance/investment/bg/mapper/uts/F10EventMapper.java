package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.dto.f10.IncrementCompanyEventDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/9/7 14:04
 * @版本：1.0
 */
@DS("db2")
public interface F10EventMapper extends BaseMapper<String> {

    @Select(
            "SELECT DISTINCT(SECCODE) FROM (SELECT a.F001D, a.SECCODE FROM XNHK0201 a where a.F001D = #{time} and a.SECCODE  in ${stockCodes} UNION ALL "
                    + " ( SELECT b.F001D, b.SECCODE FROM XNHK0202 b where b.F001D = #{time} and b.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT c.F001D, c.SECCODE FROM XNHK0203 c where c.F001D = #{time} and c.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT d.F001D, d.SECCODE FROM XNHK0204 d where d.F001D = #{time} and d.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT e.F001D, e.SECCODE FROM XNHK0205 e where e.F001D = #{time} and e.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT f.F001D, f.SECCODE FROM XNHK0206 f where f.F001D = #{time} and f.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT g.F001D, g.SECCODE FROM XNHK0207 g where g.F001D = #{time} and g.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT h.F001D, h.SECCODE FROM XNHK0208 h where h.F001D = #{time} and h.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT i.F001D, i.SECCODE FROM XNHK0209 i where i.F001D = #{time} and i.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT j.F001D, j.SECCODE FROM XNHK0210 j where j.F001D = #{time} and j.SECCODE  in ${stockCodes}) UNION ALL "
                    + " ( SELECT k.F001D, k.SECCODE FROM XNHK0210 k where k.F001D = #{time} and k.SECCODE  in ${stockCodes}) ) l ORDER BY SECCODE asc")
    Page<String> listByPageF10Event(
            Page<String> page,
            @Param("stockCodes") String stockCodes,
            @Param("time") Long time
    );

    @Select("SELECT DISTINCT(SECCODE) FROM (SELECT a.F001D, a.SECCODE FROM XNHK0201 a UNION ALL "
            + " ( SELECT b.F001D, b.SECCODE FROM XNHK0202 b ) UNION ALL "
            + " ( SELECT c.F001D, c.SECCODE FROM XNHK0203 c ) UNION ALL "
            + " ( SELECT d.F001D, d.SECCODE FROM XNHK0204 d ) UNION ALL "
            + " ( SELECT e.F001D, e.SECCODE FROM XNHK0205 e ) UNION ALL "
            + " ( SELECT f.F001D, f.SECCODE FROM XNHK0206 f ) UNION ALL "
            + " ( SELECT g.F001D, g.SECCODE FROM XNHK0207 g ) UNION ALL "
            + " ( SELECT h.F001D, h.SECCODE FROM XNHK0208 h ) UNION ALL "
            + " ( SELECT i.F001D, i.SECCODE FROM XNHK0209 i ) UNION ALL "
            + " ( SELECT j.F001D, j.SECCODE FROM XNHK0210 j ) UNION ALL "
            + " ( SELECT k.F001D, k.SECCODE FROM XNHK0210 k ) ) l where F001D = #{time}")
    List<String> listF10EventCodes(
            @Param("time") Long time
    );

    @Select(
            "SELECT SECCODE as stockCode, F001D as time FROM (SELECT a.F001D, a.SECCODE FROM XNHK0201 a where (a.Create_Date >= #{beginTime} and a.Create_Date <= #{endTime}) or (a.Modified_Date >= #{beginTime} and a.Modified_Date <= #{endTime} ) UNION ALL "
                    + "( SELECT b.F001D, b.SECCODE FROM XNHK0202 b where (b.Create_Date >= #{beginTime} and b.Create_Date <= #{endTime}) or (b.Modified_Date >= #{beginTime} and b.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT c.F001D, c.SECCODE FROM XNHK0203 c where (c.Create_Date >= #{beginTime} and c.Create_Date <= #{endTime}) or (c.Modified_Date >= #{beginTime} and c.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT d.F001D, d.SECCODE FROM XNHK0204 d where (d.Create_Date >= #{beginTime} and d.Create_Date <= #{endTime}) or (d.Modified_Date >= #{beginTime} and d.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT e.F001D, e.SECCODE FROM XNHK0205 e where (e.Create_Date >= #{beginTime} and e.Create_Date <= #{endTime}) or (e.Modified_Date >= #{beginTime} and e.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT f.F001D, f.SECCODE FROM XNHK0206 f where (f.Create_Date >= #{beginTime} and f.Create_Date <= #{endTime}) or (f.Modified_Date >= #{beginTime} and f.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT g.F001D, g.SECCODE FROM XNHK0207 g where (g.Create_Date >= #{beginTime} and g.Create_Date <= #{endTime}) or (g.Modified_Date >= #{beginTime} and g.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT h.F001D, h.SECCODE FROM XNHK0208 h where (h.Create_Date >= #{beginTime} and h.Create_Date <= #{endTime}) or (h.Modified_Date >= #{beginTime} and h.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT i.F001D, i.SECCODE FROM XNHK0209 i where (i.Create_Date >= #{beginTime} and i.Create_Date <= #{endTime}) or (i.Modified_Date >= #{beginTime} and i.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT j.F001D, j.SECCODE FROM XNHK0210 j where (j.Create_Date >= #{beginTime} and j.Create_Date <= #{endTime}) or (j.Modified_Date >= #{beginTime} and j.Modified_Date <= #{endTime} )) UNION ALL "
                    + "( SELECT k.F001D, k.SECCODE FROM XNHK0210 k where (k.Create_Date >= #{beginTime} and k.Create_Date <= #{endTime}) or (k.Modified_Date >= #{beginTime} and k.Modified_Date <= #{endTime} )) ) l")
    List<IncrementCompanyEventDTO> listIncrementF10(
            @Param("beginTime") Date beginTime,
            @Param("endTime") Date endTime
    );
}
