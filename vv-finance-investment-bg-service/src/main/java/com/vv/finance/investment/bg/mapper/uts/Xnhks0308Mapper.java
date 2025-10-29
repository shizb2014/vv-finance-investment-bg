package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0308;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 13:54
 * @Version 1.0
 */
@DS("db2")
public interface Xnhks0308Mapper extends BaseMapper<Xnhks0308> {

    /**
     * 查询最新的数据, 合并到公司动向表
     *
     * @param dbmask
     */
    @Select("select null id, SECCODE,#{code} type,F006V content, F001D releaseDate,F005D order_date,F005D date1,null date2,XDBMASK sxdbmask, md5(concat(#{code},x.SECCODE,x.F001D)) uni from vv_uts.XNHKS0308 x where x.xdbmask > #{dbmask} order by x.xdbmask")
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);

}
