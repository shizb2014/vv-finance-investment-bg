package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.trends.TransactionAlert;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0311;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:52
 * @Version 1.0
 */
@DS("db2")
public interface Xnhk0311Mapper extends BaseMapper<Xnhk0311> {


    /**
     * 关联xnhk0006和xnhk0007 查询警报类型，警报时间
     *
     * @param transactionAlertPage
     * @param stockCode
     * @param date
     * @return
     */
    @Select(" SELECT x1.SECCODE stockCode, x1.F001D releaseDate, x2.F001V alertType, x3.F001V alertCode, x1.F004N closingPrice, x1.F005N `change`, x1.F006N turnover " +
            " from xnhk0311 x1 left join xnhk0007 x2 on x1.F002V = x2.`CODE` left join xnhk0006 x3  on x1.F003V = x3.`CODE` where x1.SECCODE in ${stockCode} " +
            " and (#{date} is null or x1.F001D = #{date}) order by x1.F001D desc")
    Page<TransactionAlert> listJoin0007And0006(Page<TransactionAlert> transactionAlertPage, @Param("stockCode") String stockCode, @Param("date") Long date);

    @Select(
            "<script>" +
                    " SELECT x1.SECCODE stockCode,\n" +
                    "       x1.F001D   releaseDate," +
                    "       x2.F001V   alertType," +
                    "       x3.F001V   alertCode," +
                    "       x1.F004N   closingPrice," +
                    "       x1.F005N   `change`," +
                    "       x1.F006N   turnover" +
                    " from vv_uts.xnhk0311 x1" +
                    "         left join vv_uts.xnhk0007 x2 on x1.F002V = x2.`CODE`" +
                    "         left join vv_uts.xnhk0006 x3 on x1.F003V = x3.`CODE`" +
                    " where x1.SECCODE = #{stockCode} " +
                    " <if test=\"date != null and date != '' \" >" +
                    "   and (x1.F001D >= #{date})" +
                    " </if>" +
                    " order by x1.F001D desc"+
            "</script>"
    )
    Page<TransactionAlert> listJoin0007And00062(Page<TransactionAlert> transactionAlertPage, @Param("stockCode") String stockCode, @Param("date") Long date);


    @Select(" SELECT x1.SECCODE stockCode, x1.F001D releaseDate, x2.F001V alertType, x3.F001V alertCode, x1.F004N closingPrice, x1.F005N `change`, x1.F006N turnover " +
            " from xnhk0311 x1 left join xnhk0007 x2 on x1.F002V = x2.`CODE` left join xnhk0006 x3 on x1.F003V = x3.`CODE` where x1.SECCODE = #{stockCode} order by x1.F001D desc limit 1")
    TransactionAlert lastOneByCode(@Param("stockCode") String stockCode);

    /**
     * 查询最新的数据, 合并到公司动向表
     *
     * @param dbmask
     */
    @Select("select null id, x1.SECCODE,#{code} type,x2.F001V content,x3.F001V content2, x1.F001D releaseDate,x1.F001D order_date,x1.F001D date1,null date2,x1.XDBMASK sxdbmask, md5(concat(#{code},x1.SECCODE,x1.F001D,x1.F003V)) uni " +
            " from vv_uts.xnhk0311 x1 left join vv_uts.xnhk0007 x2 on x1.F002v = x2.CODE left join vv_uts.xnhk0006 x3 on x1.F003v = x3.CODE where x1.xdbmask > #{dbmask} order by x1.xdbmask")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);


    CompanyTrendsMergeEntity getByPrimary(@Param("type") Integer type, @Param("seccode") String seccode,@Param("F001D") Long f001d, @Param("F003V") String f003v);

}
