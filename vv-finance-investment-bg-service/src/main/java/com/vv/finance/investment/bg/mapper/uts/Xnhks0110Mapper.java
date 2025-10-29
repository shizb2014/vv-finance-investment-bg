package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhks0110;
import com.vv.finance.investment.bg.entity.uts.Xnhks0111;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: Xnhks0601Mapper
 * @Description:
 * @Author: wsliang
 */
@DS("db2")
public interface Xnhks0110Mapper extends BaseMapper<Xnhks0110> {

    @Select(" select F003D, F008V, F005V, round(sum(F009N),2) F009N from (select LEFT(F003D, 4)  F003D, F008V, F005V, F009N from xnhks0110" +
            " where SECCODE = #{stockCode} and F001V = 'F' and F009N is not null and F009N != 0 and F003D >= concat(year(DATE_SUB(CURDATE(),INTERVAL 15 year)),'0000')) x  GROUP BY F003D, F008V, F005V order by F003D")
    List<Xnhks0110> listModel(@Param("stockCode") String stockCode);

    @Select(" select max(Modified_Date) from xnhks0110 where SECCODE = #{stockCode} and F009N is not null and F001V = 'F' and F003D >= year(DATE_SUB(CURDATE(),INTERVAL 15 year)) GROUP BY SECCODE ")
    Date maxModifiedDate(@Param("stockCode") String stockCode);
}