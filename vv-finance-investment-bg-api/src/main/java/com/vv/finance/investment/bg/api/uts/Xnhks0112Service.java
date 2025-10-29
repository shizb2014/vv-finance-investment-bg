package com.vv.finance.investment.bg.api.uts;


import com.vv.finance.investment.bg.dto.uts.req.DividendDateInfoReq;
import com.vv.finance.investment.bg.dto.uts.resp.DividendDateInfoResp;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;

import java.util.Date;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
public interface Xnhks0112Service {

    /**
     * 查询股票的派息日期信息
     *
     * @param req 派息日期信息查询请求对象
     * @return 列表
     */
    List<DividendDateInfoResp> queryDividendDateInfo(DividendDateInfoReq req);

    List<Xnhks0112> getXnhk0112sIncludeSS(Date startDate, Date endDate);

    List<Xnhks0112> getXnhk0112sNotIncludeSS(Date startDate, Date endDate);

}
