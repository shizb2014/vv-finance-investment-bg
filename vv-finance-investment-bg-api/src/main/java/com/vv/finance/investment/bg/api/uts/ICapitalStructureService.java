package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalChange;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalStatistics;

/**
 * @Author: wsliang
 * @Date: 2021/9/2 10:57
 **/
public interface ICapitalStructureService {
    CapitalStatistics getCapitalStructure(String stockCode);

    PageDomain<CapitalChange> pageCapitalChange(String stockCode, SimplePageReq simplePageReq);
}
