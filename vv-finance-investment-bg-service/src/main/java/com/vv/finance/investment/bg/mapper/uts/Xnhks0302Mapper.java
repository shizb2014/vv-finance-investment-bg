package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhks0302;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2021-08-20
 */
@DS("db2")
public interface Xnhks0302Mapper extends BaseMapper<Xnhks0302> {

    @Select("select a.F004V f004v,a.F003V f003v from xnhks0302 a ,(select F004V,MAX(XDBMASK) xdbmask from xnhks0302 \n" +
            "where SECCODE = #{stockCode} group by F004V) b where a.F004V = b.F004V and a.XDBMASK = b.XDBMASK")
    List<Xnhks0302> listF003V(@Param("stockCode") String stockCode);
}
