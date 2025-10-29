package com.vv.finance.investment.bg.api.uts;


import com.vv.finance.investment.bg.entity.uts.Xnhks0314;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
public interface Xnhks0314Service {

    /**
     * 查询拆并股详情列表
     * @param stockCodeList
     * @param date
     * @return
     */
    List<Xnhks0314> getSsScList(List<String> stockCodeList, LocalDate date);
}
