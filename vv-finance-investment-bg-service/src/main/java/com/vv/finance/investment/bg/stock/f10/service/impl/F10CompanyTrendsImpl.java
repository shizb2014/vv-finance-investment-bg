package com.vv.finance.investment.bg.stock.f10.service.impl;

import com.vv.finance.investment.bg.entity.f10.trends.AcquisitionsAndMergers;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0308Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 14:07
 * @Version 1.0
 */
@Service("f10CompanyTrends")
@Slf4j
public class F10CompanyTrendsImpl {

    @Resource
    private Xnhks0308Mapper xnhks0308Mapper;

    public List<AcquisitionsAndMergers> getAcquisitionsAndMergers(String code){
        List<AcquisitionsAndMergers> acquisitionsAndMergers = new ArrayList<>();
        return null;
    }
}
