package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.common.entity.quotation.f10.ComNewShareCalendarVO;
import com.vv.finance.common.entity.quotation.f10.ComNewShareVo;
import com.vv.finance.common.entity.common.ComNewStockProspectusVo;
import com.vv.finance.investment.bg.entity.information.NewShareInvestorInfo;
import com.vv.finance.investment.bg.entity.information.NewShareVo;
import com.vv.finance.investment.bg.entity.uts.Xnhks0503;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author chenyu
 * @since 2021-03-30
 */
@DS("db2")
public interface Xnhks0503Mapper extends BaseMapper<Xnhks0503> {

    /**
     * 获取文件id
     *
     * @param code
     * @return
     */
    List<String> getLineId(@Param("code") String code);

    @Select(" select x1.SECCODE SECCODE,x1.SECCODE stockCode, x2.F003V stockName, x1.F008N minPrice, x1.F009N maxPrice, x1.F044V stockLink, " +
            " x1.F021N shareNum,x1.F013N entranceFee, CONCAT(UNIX_TIMESTAMP(x1.F005D),'000') subscribeStartDate, " +
            " CONCAT(UNIX_TIMESTAMP(x1.F006D),'000') subscribeEndDate, CONCAT(UNIX_TIMESTAMP(x3.F016D),'000') publicityDate, " +
            " CONCAT(UNIX_TIMESTAMP(x1.F002D),'000') marketDate from xnhks0503 x1 left join xnhks0501 x2 " +
            " on x1.SECCODE = x2.SECCODE and x1.F002D = x2.F002D left join XNHKE0502 x3 on x1.SECCODE = x3.SECCODE and x1.F002D = x3.F002D " +
            " where x1.F002D >= CURRENT_DATE and x1.F031N is not null order by x1.F002D desc, x1.Create_Date desc")
    Page<NewShareVo> pageNewShareVo(Page<NewShareVo> page);

//    @Select(" select x1.SECCODE SECCODE,x1.SECCODE stockCode, x2.F003V stockName, x1.F008N minPrice, x1.F009N maxPrice, x1.F011N finalPrice, x1.F044V stockLink, " +
//            " x1.F021N shareNum,x1.F013N entranceFee, CONCAT(UNIX_TIMESTAMP(x1.F005D),'000') subscribeStartDate, " +
//            " CONCAT(UNIX_TIMESTAMP(x1.F006D),'000') subscribeEndDate, CONCAT(UNIX_TIMESTAMP(x3.F016D),'000') publicityDate, " +
//            " CONCAT(UNIX_TIMESTAMP(x1.F002D),'000') marketDate from xnhks0503 x1 left join xnhks0501 x2 " +
//            " on x1.SECCODE = x2.SECCODE and x1.F002D = x2.F002D left join XNHKS0502 x3 on x1.SECCODE = x3.SECCODE and x1.F002D = x3.F002D " +
//            " where x1.F002D >= subdate(curdate(),date_format(curdate(),'%w')+6) and x1.F031N is not null and x1.F001V = 'Y' and x2.F001V = 'Y' and x3.F001V = 'Y'  and x1.SECCODE in (\n" +
//            "            <foreach collection=\"list\" item=\"code\" separator=\",\">\n" +
//            "                #{code}\n" +
//            "            </foreach>\n" +
//            "            )" +
//            " order by x1.F002D desc, x1.XDBMASK desc, x2.XDBMASK desc, x3.XDBMASK desc")
    List<ComNewShareVo> listNewShareVo(@Param("list") List<String> codes);

    List<ComNewShareVo> listNewShare(@Param("date")String date);

    Page<ComNewShareVo> listNewShareVoByTime(@Param("startDate")String startDate, @Param("endDate")String endDate, @Param("list") List<String> codes, Page<NewShareVo> page);

    List<ComNewShareVo> listNewShareVoByType(@Param("startDate")String startDate, @Param("endDate")String endDate);
    /**
     * 查询基石投资者信息
     *
     * @param stockCodes
     */
    @Select("SELECT " +
            "x1.F003V NAME, " +
            "x1.F004V currency, " +
            "x2.F001V currencyName, " +
            "x1.F005N amount, " +
            "x1.F006N planSubscriptionNum, " +
            "x1.F007N planRate, " +
            "x1.F010N actualRate, " +
            "x1.F009N actualSubscriptionNum, " +
            "CONCAT(UNIX_TIMESTAMP(x1.F012D),'000') expiredDate " +
            "FROM " +
            "XNHKS0507 x1 " +
            "LEFT JOIN XNHK0008 x2 ON x1.F004V = x2.`CODE` " +
            "where x1.SECCODE = #{stockCodes}")
    List<NewShareInvestorInfo> queryInvestorsByStockCodes(@Param("stockCodes") String stockCodes);

    /**
     * @param stockCodes
     * @return
     */
    @Select("SELECT F009V FROM `XNHKS0501` where SECCODE = #{stockCodes} order by XDBMASK desc limit 1")
    String querySponsor(@Param("stockCodes") String stockCodes);

    /**
     * 包销商信息
     *
     * @param stockCodes
     * @return
     */
    @Select(" SELECT F004V,F005V,F006V,F007V,F008V,F009V,F010V,F011V,F012V,F013V,F014V,F015V,F016V,F017V,F018V,F019V,F020V,F021V,F022V,F023V,F024V,F025V,F026V,F027V,F028V,F029V " +
            " FROM `XNHKS0505` where SECCODE = #{stockCodes} order by XDBMASK desc limit 1 ")
    Map<String, String> queryUnderwriters(@Param("stockCodes") String stockCodes);

    @Select(" select x1.SECCODE SECCODE,x1.SECCODE stockCode, x2.F003V stockName, x1.F008N minPrice, x1.F009N maxPrice, x1.F044V stockLink, x1.F005D subscribeStartDate, x1.F006D subscribeEndDate," +
            " x3.F016D publicityDate, x1.F002D marketDate, x1.Modified_Date modifiedDate from xnhks0503 x1 left join xnhks0501 x2 on x1.SECCODE = x2.SECCODE left join XNHKE0502 x3 on x1.SECCODE = x3.SECCODE " +
            " where x3.F016D > CURRENT_DATE order by x3.F016D desc, x1.Modified_Date desc")
    Page<NewShareVo> pageNewShareVoByTime(Page<NewShareVo> page, @Param("newsTime") String newsTime);

    @Select({
            "<script>",
            "SELECT a.* FROM `xnhks0503` a INNER JOIN(select SECCODE, max(ifnull(Modified_Date,0)) date  from `xnhks0503` GROUP BY SECCODE) b on ifnull(a.Modified_Date,0) = b.date and b.SECCODE = a.SECCODE where a.SECCODE in",
            "<foreach collection='codes' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<Xnhks0503> selectLastedNewStock(@Param("codes") List<String> codes);

    /**
     * @param stockCode
     * @return
     */
    @Select("select * from xnhks0503 where SECCODE = #{stockCode} order by XDBMASK desc limit 1")
    Xnhks0503 queryIpoSummary(@Param("stockCode") String stockCode);

    List<ComNewStockProspectusVo> findNewStockProspectusInfo(@Param("time")Integer time,@Param("marketStatus")Integer marketStatus);
}
