package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.entity.quotation.f10.ComCompanyTrendVo;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0317;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 10:33
 * @Version 1.0
 */
@DS("db2")
public interface Xnhks0317Mapper extends BaseMapper<Xnhks0317> {

    /**
     * 查询最新的数据, 合并到公司动向表
     *
     * @param dbmask
     */
    @Select(" select null id, x1.SECCODE,#{code} type,'' content, x1.F001D releaseDate, x1.F001D order_date,x1.F002D date1,x1.XDBMASK sxdbmask, md5(concat(#{code},x1.SECCODE,x1.F001D,x1.F002D)) uni from " +
            " vv_uts.XNHKS0317 x1 where x1.xdbmask > #{dbmask} order by x1.xdbmask")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);

    /**
     * 查询复牌数据
     *
     */
    @Select(" select SECCODE, F001D releaseDate, F001D order_date, F003D date1, F002D date2, md5(concat(#{code},SECCODE,F001D,F003D)) uni from vv_uts.XNHK0318 where SECCODE in ${stocks}")
    List<CompanyTrendsMergeEntity> listSuspension(@Param("stocks") String stocks, @Param("code") Integer code);


    CompanyTrendsMergeEntity getByPrimary(@Param("type") Integer type, @Param("seccode") String seccode,@Param("F002D") Long f002d);

    @Select(" select count(*) from  vv_uts.XNHKS0317 where SECCODE = #{code} and F002D = #{date}")
    Integer countByCodeAndDate(@Param("code") String code, @Param("date") Long date);

    @Select("<script>" +
            "select seccode stockCode, '停牌' content from xnhks0317 c\n" +
            "        <where>\n" +
            "       <if test=\"startTime != null\">\n" +
            "                AND <![CDATA[ c.create_date >= #{startTime,jdbcType=DATE}  ]]>\n" +
            "       </if>\n" +
            "       <if test=\"endTime != null\">\n" +
            "                AND <![CDATA[ c.create_date <= #{endTime,jdbcType=DATE}  ]]>\n" +
            "       </if>\n" +
            "       and c.seccode in\n" +
            "       <foreach item=\"item\" collection=\"list\" separator=\",\" open=\"(\" close=\")\" index=\"\">\n" +
            "           #{item}\n" +
            "       </foreach>\n" +
            "        </where>\n" +
            "</script>")
    List<ComCompanyTrendVo> listNotifySuspension(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("list") List<String> codes);
}
