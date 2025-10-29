package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk1002;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("db2")
public interface Xnhk1002Mapper extends BaseMapper<Xnhk1002> {

    /**
     * 查询指数成分股码表
     * @param index
     * @return
     */
    @Select("SELECT members.F001V as F001V, indexRelated.F001V as `SECCODE` FROM xnhk1003 indexRelated " +
            " LEFT JOIN xnhk1002 members ON indexRelated.SECCODE = members.SECCODE WHERE indexRelated.F001V in ${index}")
    List<Xnhk1002> listMembersAndIndex(@Param("index") String index);
}
