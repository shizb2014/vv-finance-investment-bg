package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("db2")
public interface Xnhk0207Mapper extends BaseMapper<Xnhk0207> {

    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);

    CompanyTrendsMergeEntity getByPrimary(@Param("type") Integer type, @Param("seccode") String seccode, @Param("F002D") Long f002d, @Param("F006V") String f006v);


}