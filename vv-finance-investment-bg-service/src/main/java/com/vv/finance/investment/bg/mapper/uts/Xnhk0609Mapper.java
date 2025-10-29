package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk0609;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("db2")
public interface Xnhk0609Mapper extends BaseMapper<Xnhk0609> {
    /**
     * 查询每日经纪商前5持股数据，并按倒序排列
     * @param code
     * @param f001d
     * @return
     */
    Xnhk0609 queryBrokerTop5Sum(@Param("code") String code, @Param("f001d") Long f001d);

    /**
     * 查询每日经纪商前10持股数据，并按倒叙排列
     * @param code
     * @param f001d
     * @return
     */
    Xnhk0609 queryBrokerTop10Sum(@Param("code") String code, @Param("f001d") Long f001d);

    /**
     * 查询每日经纪商前20持股数据，并按倒叙排列
     * @param code
     * @param f001d
     * @return
     */
    Xnhk0609 queryBrokerTop20Sum(@Param("code") String code, @Param("f001d") Long f001d);


    /**
     * 获得0609表中经纪商名单
     * @return
     */
    List<String> getBrokerIdList();


}
