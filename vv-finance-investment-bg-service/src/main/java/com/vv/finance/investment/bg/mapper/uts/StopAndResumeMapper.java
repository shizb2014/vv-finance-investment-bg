package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.trends.StopAndResume;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 16:35
 * @Version 1.0
 */
@DS("db2")
public interface StopAndResumeMapper extends BaseMapper<StopAndResume> {

    @Select(
            "select a.F001D as releaseDate,a.F002D as stopDate,b.F002D as resumeDate,a.F003v as stopReason, a.SECCODE stockCode "
                    + "from XNHKS0317 a LEFT JOIN  XNHK0318 b " + "on a.SECCODE=b.SECCODE and a.F002D=b.F003D "
                    + "where a.SECCODE in ${stockCodes} and (#{time} is null or a.F001D = #{time} or a.F002D = #{time} or b.F002D = #{time}) order by a.F001D desc")
    Page<StopAndResume> getStopAndResume(
            Page<StopAndResume> page,
            @Param("stockCodes") String stockCodes,
            @Param("time") Long time
    );

    @Select(
            "<script>" +
                    " select a.F001D as releaseDate, a.F002D as stopDate, b.F002D as resumeDate, a.F003v as stopReason, a.SECCODE stockCode" +
                    " from XNHKS0317 a LEFT JOIN XNHK0318 b on a.SECCODE = b.SECCODE and a.F002D = b.F003D" +
                    " where a.SECCODE = #{stockCode} " +
                    " <if test=\"date != null and date != '' \" >" +
                    "   and (a.F001D >= #{date} or b.F001D >= #{date})" +
                    " </if>" +
                    " order by a.F001D desc" +
            "</script>"
    )
    Page<StopAndResume> getStopAndResume2(Page<StopAndResume> page, @Param("stockCode") String stockCode, @Param("date") Long date);

    @Select("select a.SECCODE as stockCode from XNHKS0317 a LEFT JOIN XNHK0318 b "
            + "on a.SECCODE=b.SECCODE and a.F002D=b.F003D "
            + "where #{time} is null or a.F001D = #{time} or a.F002D = #{time} or b.F002D = #{time}")
    List<StopAndResume> getStopAndResumeStockCodes(@Param("time") Long time);

    @Select(
            "select a.F001D as releaseDate,a.F002D as stopDate,b.F002D as resumeDate,a.F003v as stopReason, a.SECCODE stockCode "
                    + "from XNHKS0317 a LEFT JOIN  XNHK0318 b on a.SECCODE=b.SECCODE and a.F002D=b.F003D "
                    + " where  (a.Create_Date >= #{beginTime} and a.Create_Date <= #{endTime}) or (b.Create_Date >= #{beginTime} and b.Create_Date <= #{endTime}) or  (a.Modified_Date >= #{beginTime} and a.Modified_Date <= #{endTime}) or (b.Modified_Date >= #{beginTime} and b.Modified_Date <= #{endTime})")
    List<StopAndResume> getIncrementStopAndResume(
            @Param("beginTime") Date beginTime,
            @Param("endTime") Date endTime
    );

    @Select(
            "select a.F001D as releaseDate,a.F002D as stopDate,b.F002D as resumeDate,a.F003v as stopReason, a.SECCODE stockCode "
                    + "from XNHKS0317 a LEFT JOIN  XNHK0318 b on a.SECCODE=b.SECCODE and a.F002D=b.F003D "
                    + " where a.SECCODE = #{stockCode} order by a.F001D desc limit 1")
    StopAndResume lastOneByStock(@Param("stockCode") String stockCode);

}
