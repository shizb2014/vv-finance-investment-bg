package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalStatistics;
import com.vv.finance.investment.bg.entity.uts.Xnhk0605;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@DS("db2")
public interface Xnhk0605Mapper extends BaseMapper<Xnhk0605> {

    @Select(" select F001D updateDate, F003N issuedCirculating, F005N freelyCirculating, F004N nonFreeCirculating, F002V type, '' lastTime from xnhk0605 where SECCODE = #{stockCode} limit 1")
    CapitalStatistics getCapitalStructure(@Param("stockCode") String stockCode);

    @Select("select F001D updateDate, F003N issuedCirculating, F005N freelyCirculating, F004N nonFreeCirculating, F002V type, '' lastTime from xnhk0605 where SECCODE = #{stockCode}")
    Page<CapitalStatistics> pageCapitalStructure(Page<CapitalStatistics> page, @Param("stockCode") String stockCode);
}
