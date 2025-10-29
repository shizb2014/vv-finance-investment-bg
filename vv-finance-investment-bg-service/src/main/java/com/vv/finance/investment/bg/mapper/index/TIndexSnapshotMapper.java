package com.vv.finance.investment.bg.mapper.index;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.index.TIndexSnapshot;

import java.util.List;

/**
 * @ClassName: TIndexSnapshotMapper
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/29   14:28
 */
public interface TIndexSnapshotMapper extends IndexBaseMapper, BaseMapper<TIndexSnapshot> {

    /**
     * 获取跑马灯指数
     * @param code
     * @return
     */
    public TIndexSnapshot getLampIndex(String code);
}