package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.domain.filter.EnumValues;
import com.vv.finance.investment.bg.entity.uts.Xnhk1301;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("db2")
public interface Xnhk1301Mapper extends BaseMapper<Xnhk1301> {

    /**
     * 查询所有的板块和code
     *
     * @return
     */
    @Select("select distinct `F001V` code, `F002V` name from xnhk1301")
    List<EnumValues> listConcepts();
    
}