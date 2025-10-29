package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk0901;
import com.vv.finance.investment.bg.entity.uts.Xnhks0601;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lh.sz
 * @since 2022-01-19
 */
@DS("db2")
public interface Xnhk0901Mapper extends BaseMapper<Xnhk0901> {


    @Select("SELECT SECCODE, MAX( F003N ) AS F003N, MIN( F004N ) AS F004N FROM `xnhk0901`  WHERE SECCODE IN ${codes} GROUP BY SECCODE")
    List<Xnhk0901> getXnhk09011List(@Param("codes")String codes);

}
