package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk0603;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lh.sz
 * @since 2021-11-12
 */
@DS("db2")
public interface Xnhk0603Mapper extends BaseMapper<Xnhk0603> {

    /**
     * 选择最新报告日期
     *
     * @param xnhksCodes XNHK代码
     * @return {@link String}
     */
    @Select(
            "<script>" +
                    " SELECT max(F001D)" +
                    " from xnhk0603" +
                    " where 1 = 1" +
                    "<if test=\"list != null and list.size() > 0\">\n" +
                    "    and SECCODE in\n" +
                    "    <foreach collection=\"list\" separator=\",\" open=\"(\" close=\")\" item=\"item\">\n" +
                    "        #{item}\n" +
                    "    </foreach>\n" +
                    "</if>" +
            "</script>"
    )
    String selectNewestReportDate(@Param("list") List<String> xnhksCodes);

    /**
     * 查询最近记录
     *
     * @param newestDate 最新日期
     * @param codeList   股票代码列表
     * @return {@link List}<{@link Xnhk0603}>
     */
    @Select(
            "<script>" +
                "SELECT * FROM (\n" +
                "    SELECT *, ROW_NUMBER() OVER (PARTITION BY SECCODE ORDER BY F001D desc ) AS row_num\n" +
                "    FROM xnhk0603 where <![CDATA[ F001D < #{date} ]]>\n" +
                "    <if test=\"list != null and list.size() > 0\">\n" +
                "        and SECCODE in\n" +
                "        <foreach collection=\"list\" separator=\",\" open=\"(\" close=\")\" item=\"item\">\n" +
                "            #{item}\n" +
                "        </foreach>\n" +
                "    </if>\n" +
                ") AS temp_table\n" +
                "WHERE <![CDATA[ row_num = 1  ]]>" +
            "</script>"
    )
    List<Xnhk0603> selectLatestRecords(@Param("date") String newestDate, @Param("list") List<String> codeList);
}
