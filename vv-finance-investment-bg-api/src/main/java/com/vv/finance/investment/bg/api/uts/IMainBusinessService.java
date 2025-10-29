package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.investment.bg.entity.f10.mainBusiness.MainBusinessData;
import com.vv.finance.investment.bg.entity.f10.shareholder.EquityChange;
import com.vv.finance.investment.bg.entity.f10.shareholder.MainShareholdingVo;
import com.vv.finance.investment.bg.entity.f10.shareholder.PopChanges;
import com.vv.finance.investment.bg.entity.f10.shareholder.ShareholdingPop;

import java.util.List;

/**
 * @Author: wsliang
 * 股本信息
 * @Date: 2021/9/6 11:06
 **/
public interface IMainBusinessService {

    /**
     * 主营业务(获取数据)
     * @param stockCode
     * @return
     */
    MainBusinessData getBusinessData(String stockCode);

    /**
     * 获取颜色池
     * @return
     */
    List<String> getBusinessSort();
}
