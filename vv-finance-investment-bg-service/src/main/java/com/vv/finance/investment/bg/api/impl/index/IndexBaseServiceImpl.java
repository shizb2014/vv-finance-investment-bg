package com.vv.finance.investment.bg.api.impl.index;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.finance.investment.bg.mapper.index.IndexBaseMapper;

import java.util.List;

/**
 * @ClassName: IndexBaseServiceImpl
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   10:23
 */
public abstract class IndexBaseServiceImpl {

    /**
     * 获取实体类列表
     *
     * @param baseMapper
     * @param queryWrapper
     * @param <T>
     * @return
     */
    protected <T> List<T> getListEntity(BaseMapper<T> baseMapper, QueryWrapper<T> queryWrapper) {
        return baseMapper.selectList(queryWrapper);
    }

    protected <T> List<T> getPageListEntity(BaseMapper<T> baseMapper, IPage<T> page, QueryWrapper<T> queryWrapper) {
        return baseMapper.selectPage(page,queryWrapper).getRecords();
    }

    /**
     * 批量保存或更新数据
     *
     * @param <T>
     * @param entities
     * @param baseMapper
     */
    protected <T> void batchSaveOrUpdate(List<T> entities, IndexBaseMapper<T> baseMapper) {
        baseMapper.batchInsert(entities);
    }


    /**
     * 保存或更新数据
     *
     * @param <T>
     * @param entity
     * @param baseMapper
     * @param updateWrapper
     */
    protected <T> void saveOrUpdate(T entity, BaseMapper<T> baseMapper, UpdateWrapper<T> updateWrapper) {

        boolean result = SqlHelper.retBool(baseMapper.update(entity, updateWrapper));
        if (!result) {
            baseMapper.insert(entity);
        }
    }

}
