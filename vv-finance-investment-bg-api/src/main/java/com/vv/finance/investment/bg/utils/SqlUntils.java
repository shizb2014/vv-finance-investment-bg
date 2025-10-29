package com.vv.finance.investment.bg.utils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;

import java.util.Date;

public class SqlUntils {
    /**
     * 保存或更新数据
     *
     * @param <T>
     * @param entity
     * @param baseMapper
     * @param updateWrapper
     */
    public static  <T extends BaseEntity> void saveOrUpdate(T entity, BaseMapper<T> baseMapper, UpdateWrapper<T> updateWrapper) {

        boolean result = SqlHelper.retBool(baseMapper.update(entity, updateWrapper));
        if (!result) {
            entity.setCreateTime(new Date());
            baseMapper.insert(entity);
        }
    }
}
