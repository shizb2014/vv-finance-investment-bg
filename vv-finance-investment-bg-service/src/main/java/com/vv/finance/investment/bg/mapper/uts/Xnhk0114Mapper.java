package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalChange;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalStatistics;
import com.vv.finance.investment.bg.entity.uts.Xnhk0114;
import com.vv.finance.investment.bg.entity.uts.Xnhk0605;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@DS("db2")
public interface Xnhk0114Mapper extends BaseMapper<Xnhk0114> {

    @Select(" select x1.F001D releaseDate, x1.F002D changeDate, x1.F003V type, round(x1.F012N,2) capital, round(x1.F013N,2) changeCapital, round(x1.F014N,5) changeRatio, x2.F001V changeReason " +
            " from xnhk0114 x1 left join XNHK0016 x2 on x1.F015V = x2.CODE where x1.SECCODE = #{stockCode} order by releaseDate desc, x1.F002D desc, field(x1.F003V, 'S', 'H', 'P', 'O', 'T')")
    Page<CapitalChange> pageCapitalChange(Page<CapitalChange> page, @Param("stockCode") String stockCode);

    @Select("select F002D from xnhk0114 where SECCODE = #{stockCode} order by F002D desc limit 1")
    String lastF002D(@Param("stockCode") String stockCode);

}
