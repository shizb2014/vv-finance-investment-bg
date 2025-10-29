package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.entity.quotation.f10.ComCompanyTrendVo;
import com.vv.finance.investment.bg.entity.uts.Xnhk0318;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:52
 * @Version 1.0
 */
@DS("db2")
public interface Xnhk0318Mapper extends BaseMapper<Xnhk0318> {

    @Select("<script>" +
            "select seccode stockCode,'复牌' content from xnhk0318 c\n" +
            "        <where>\n" +
            "            <if test=\"startTime != null\">\n" +
            "                AND <![CDATA[ c.create_date >= #{startTime,jdbcType=DATE} ]]>\n" +
            "            </if>\n" +
            "            <if test=\"endTime != null\">\n" +
            "                AND <![CDATA[ c.create_date <= #{endTime,jdbcType=DATE} ]]>\n" +
            "            </if>\n" +
            "            and c.seccode in\n" +
            "            <foreach item=\"item\" collection=\"list\" separator=\",\" open=\"(\" close=\")\" index=\"\">\n" +
            "                #{item}\n" +
            "            </foreach>\n" +
            "        </where>" +
            "</script>")
    List<ComCompanyTrendVo> listNotifyResumption(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("list") List<String> codes);
}
