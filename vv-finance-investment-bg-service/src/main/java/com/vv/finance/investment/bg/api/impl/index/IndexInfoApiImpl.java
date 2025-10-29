package com.vv.finance.investment.bg.api.impl.index;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.index.IndexInfoApi;
import com.vv.finance.investment.bg.entity.index.TIndexInfo;
import com.vv.finance.investment.bg.mapper.index.TIndexInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description: IndexInfoApiImpl
 * date: 2022/6/20 16:46
 * author: fenghua.cai
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class IndexInfoApiImpl implements IndexInfoApi {

    @Autowired
    private TIndexInfoMapper tIndexInfoMapper;

    @Override
    public ResultT<TIndexInfo> queryIndexInfo(String code) {
        return ResultT.success(tIndexInfoMapper.selectOne(new QueryWrapper<TIndexInfo>().eq("code", code)));
    }
}
