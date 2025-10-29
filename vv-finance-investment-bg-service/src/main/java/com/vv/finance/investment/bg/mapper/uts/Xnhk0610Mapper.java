package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.broker.allBroker.AllBrokerRank;
import com.vv.finance.investment.bg.entity.uts.Xnhk0610;

import java.util.List;

@DS("db2")
public interface Xnhk0610Mapper extends BaseMapper<Xnhk0610> {
    /**
     * 查询所有经纪商的id以及名字
     * @return
     */
    List<AllBrokerRank> getIdAndName();
}
