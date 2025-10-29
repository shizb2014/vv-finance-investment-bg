package com.vv.finance.investment.bg.api.impl.uts;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.investment.bg.api.uts.Xnhks0314Service;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0314Mapper;
import com.vv.finance.investment.bg.utils.LongDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class Xnhks0314ServiceImpl implements Xnhks0314Service {

    @Resource
    private Xnhks0314Mapper xnhks0314Mapper;

    @Override
    public List<Xnhks0314> getSsScList(List<String> stockCodeList, LocalDate date){
        long longDate = LongDateUtil.getLongDate(date);
        LambdaQueryWrapper<Xnhks0314> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(Xnhks0314::getSeccode, stockCodeList)
                        .eq(Xnhks0314::getF006d, longDate);
       return xnhks0314Mapper.selectList(queryWrapper);
    }
}