package com.vv.finance.investment.bg.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.entity.uts.Xnhk0002;
import com.vv.finance.investment.bg.entity.uts.Xnhk0008;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0002Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0008Mapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author hamilton
 * @date 2021/8/19 16:22
 */
@Component
@RequiredArgsConstructor
public class F10CommonCache {
    private final Xnhk0008Mapper xnhk0008Mapper;
    private final Xnhk0002Mapper xnhk0002Mapper;


    public Xnhk0002 getXnhk0002Mapper(String code){
        return xnhk0002Mapper.selectOne(new QueryWrapper<Xnhk0002>().eq("code",code));
    }
    public Xnhk0008 getXnhk0008Mapper(String code){
        return xnhk0008Mapper.selectOne(new QueryWrapper<Xnhk0008>().eq("code",code));
    }
    @Cacheable(value = RedisKeyConstants.BG_F10_INFO,unless = "#result ==null ",keyGenerator = "keyGenerator")
    public String simpleChineseCurrency(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        return getXnhk0008Mapper(code).getF001v();
    }

    @Cacheable(value = RedisKeyConstants.BG_F10_INFO,keyGenerator = "keyGenerator")
    public String simpleChineseType(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        return getXnhk0002Mapper(code).getF001v();
    }
}
