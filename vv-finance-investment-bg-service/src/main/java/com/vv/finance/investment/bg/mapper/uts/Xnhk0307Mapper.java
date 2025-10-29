package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0307;
import com.vv.finance.investment.bg.entity.uts.Xnhks0308;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 13:54
 * @Version 1.0
 */
@DS("db2")
public interface Xnhk0307Mapper extends BaseMapper<Xnhk0307> {
    //获取股票转板信息
    @Select("select t1.* from xnhk0307 t1 join XNHKS0101 t2 on t1.SECCODE = t2.SECCODE   where t1.SECCODE = #{code} and t2.F007D <=t1.F003D and t1.F003D <= #{date} order by t1.F003D asc")
    List<Xnhk0307> findAllConversionMarketInfo(@Param("code")  String code,@Param("date")  String date);
}
