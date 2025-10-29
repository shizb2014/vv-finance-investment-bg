package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0309;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:49
 * @Version 1.0
 */
@DS("db2")
public interface Xnhks0309Mapper extends BaseMapper<Xnhks0309> {

    /**
     * 查询最新的数据, 合并到公司动向表
     *
     * @param dbmask
     */
    @Select(" select null id, SECCODE,#{code} type,F004V content, F001D releaseDate, F001D order_date,F003D date1,null date2,XDBMASK sxdbmask, md5(concat(#{code},x1.SECCODE,x1.F002D)) uni " +
            " from vv_uts.XNHKS0309 x1 where x1.xdbmask > #{dbmask} order by x1.xdbmask")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);
}
