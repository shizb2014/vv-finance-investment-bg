package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.common.entity.quotation.f10.ComCompanyTrendVo;
import com.vv.finance.investment.bg.entity.f10.trends.GeneralMeeting;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0310;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:51
 * @Version 1.0
 */
@DS("db2")
public interface Xnhks0310Mapper extends BaseMapper<Xnhks0310> {

    @Select(" SELECT x1.F001D  releaseDate, x1.F002V eventNo, x1.F003D meetingDate, x2.F001V meetingType, x1.F006V eventDetail, x1.SECCODE stockCode " +
            " from xnhks0310 x1 left join xnhk0009 x2 on x1.F005V = x2.`CODE` where x1.SECCODE in ${stockCode} and (#{date} is null or x1.F001D = #{date} " +
            " or x1.F003D = #{date}) order by x1.F001D desc")
    Page<GeneralMeeting> listJoin0009(Page<GeneralMeeting> xnhks0310Page, @Param("stockCode") String stockCode, @Param("date") Long date);

    @Select(
            "<script>" +
                    " SELECT x1.F001D   releaseDate," +
                    "       x1.F002V   eventNo," +
                    "       x1.F003D   meetingDate," +
                    "       x2.F001V   meetingType," +
                    "       case x1.F006V when '不适用' then '无' else x1.F006V end as eventDetail," +
                    "       x1.SECCODE stockCode" +
                    " from xnhks0310 x1" +
                    "         left join xnhk0009 x2 on x1.F005V = x2.`CODE`" +
                    " where x1.SECCODE = #{stockCode} " +
                    " <if test=\"date != null and date != '' \" >" +
                    "   and (x1.F001D >= #{date})" +
                    " </if>" +
                    " order by x1.F001D desc" +
            "</script>"
    )
    Page<GeneralMeeting> listJoin00092(Page<GeneralMeeting> xnhks0310Page, @Param("stockCode") String stockCode, @Param("date") Long date);

    @Select(" SELECT x1.F001D  releaseDate, x1.F002V eventNo, x1.F003D meetingDate, x2.F001V meetingType, x1.F006V eventDetail, x1.SECCODE stockCode " +
            " from xnhks0310 x1 left join xnhk0009 x2 on x1.F005V = x2.`CODE` where x1.SECCODE = #{stockCode} order by x1.F001D desc limit 1 ")
    GeneralMeeting lastOneByCode(@Param("stockCode") String stockCode);

    /**
     * 查询最新的数据, 合并到公司动向表
     *
     * @param dbmask
     */
    @Select(" select null id, x.SECCODE,#{code} type,x2.F001V content, x.F001D releaseDate,x.F003D order_date,x.F003D date1,null date2,x.XDBMASK sxdbmask, md5(concat(#{code},x.SECCODE,x.F002V)) uni from vv_uts.XNHKS0310 x " +
            " left join vv_uts.xnhk0009 x2 on x.F005V = x2.CODE where x.xdbmask > #{dbmask} order by x.xdbmask")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);



    CompanyTrendsMergeEntity getByPrimary(@Param("type") Integer type, @Param("seccode") String seccode, @Param("F002V") String f002v);

    @Select("SELECT x1.seccode stockCode, x1.f003d releaseDate, x2.f001v content\n" +
            "from vv_uts.xnhks0310 x1 left join vv_uts.xnhk0009 x2 on x1.F005V = x2.`code`\n" +
            "where x1.f003d = #{date} and x1.seccode in ${stockCode}")
    List<ComCompanyTrendVo> listNotifyGeneralMeeting(@Param("stockCode") String stockCode, @Param("date") Long date);
}
