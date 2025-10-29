package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.dto.f10.NewStockEventTimeLineDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/9/6 11:45
 * @版本：1.0
 */
@DS("db2")
public interface NewStockTimeLineMapper extends BaseMapper<NewStockEventTimeLineDTO> {
    @Select("SELECT distinct  a.SECCODE AS stockCode, a.F003V as stockName, b.F005D AS applyTime, c.F005D AS applyStartTime, \n"
        + "c.F006D AS applyEndTime,b.F013D AS setPriceTime,b.F016D AS announcedTime, \n"
        + "a.F002D AS marketTime,b.F010D AS endTime FROM  (select e.SECCODE, e.F003V, e.F002D from XNHKS0501 e \n"
        + "INNER JOIN(select SECCODE, max(ifnull(Modified_Date,0)) date, max(F002D) f002D from `xnhks0501` x1 GROUP BY SECCODE) d on ifnull(e.Modified_Date,0) = d.date and d.SECCODE = e.SECCODE and e.F002d = d.F002D) a\n"
        + "LEFT JOIN (select f.SECCODE,f.F005D,f.F013D,f.F016D,f.F010D from XNHKS0502 f INNER JOIN(select SECCODE, max(ifnull(Modified_Date,0)) date from `XNHKS0502` GROUP BY SECCODE) g on ifnull(f.Modified_Date,0) = g.date and f.SECCODE = g.SECCODE) b ON a.SECCODE = b.SECCODE\n"
        + "LEFT JOIN (select h.SECCODE,h.F005d,h.F006D from XNHKS0503 h INNER JOIN(select SECCODE, max(ifnull(Modified_Date,0)) date from `XNHKS0503` GROUP BY SECCODE) i on  ifnull(h.Modified_Date,0) = i.date and h.SECCODE = i.SECCODE) c ON a.SECCODE = c.SECCODE "
        + "WHERE b.F005D = #{time} or c.F005D = #{time} or c.F006D = #{time} or b.F013D = #{time} or b.F016D = #{time} or a.F002D = #{time} order by a.F002D desc")
    List<NewStockEventTimeLineDTO> listNewStockTimeLine(
        @Param("time") Long time
    );

    @Select(
        "SELECT a.SECCODE AS stockCode,a.F003V AS stockName,b.F005D AS applyTime,c.F005D AS applyStartTime,c.F006D AS applyEndTime, "
            + "b.F013D AS setPriceTime,b.F016D AS announcedTime, a.F002D AS marketTime,b.F010D AS endTime,a.Create_Date,a.Modified_Date  "
            + "FROM XNHKS0501 a LEFT JOIN XNHKS0502 b ON a.SECCODE = b.SECCODE LEFT JOIN XNHKS0503 c ON a.SECCODE = c.SECCODE WHERE "
            + " (a.Create_Date >= #{beginTime} and a.Create_Date <= #{endTime}) or (b.Create_Date >= #{beginTime} and b.Create_Date <= #{endTime}) or (c.Create_Date >= #{beginTime} and c.Create_Date <= #{endTime}) and (a.Modified_Date >= #{beginTime} and a.Modified_Date <= #{endTime}) or (b.Modified_Date >= #{beginTime} and b.Modified_Date <= #{endTime}) or (c.Modified_Date >= #{beginTime} and c.Modified_Date <= #{endTime})")
    List<NewStockEventTimeLineDTO> listIncrementNewStockTimeLine(
        @Param("beginTime") Date beginTime,
        @Param("endTime") Date endTime
    );
}
