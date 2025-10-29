package com.vv.finance.investment.bg.api.uts;


import com.vv.finance.investment.bg.dto.uts.resp.DividendAmountInfoResp;
import com.vv.finance.investment.bg.dto.uts.resp.RightsIssueEventInfoDTO;
import com.vv.finance.investment.bg.dto.uts.resp.Xnhk0127DTO;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
public interface Xnhk0127Service {

    /**
     * 获取指定日期的所有派息的股票
     * @param queryDate 示例：20010514
     * @return
     */
    List<DividendAmountInfoResp> queryDividendStock(Long queryDate);

    /**
     * 获取供股信息
     * @param stockCode 股票代码
     * @param exRightDate 除权日
     * @param type 除权类型
     * @return 供股信息
     */
    List<RightsIssueEventInfoDTO> getRightsIssueInfo(String stockCode, LocalDate exRightDate, String type);

    /**
     * 获取指定日期全部除权事件
     * @param queryDate
     * @return
     */
    List<Xnhk0127> getAllXrStockInfo(LocalDate queryDate);

    /**
     * 获取除权类型超过一种的错误记录(检查数据，需要融聚汇修复)
     * @return
     */
    List<Xnhk0127> getErrorXrRecords();

    /**
     * 获取近N日待补偿的除权记录(CD,SD)
     * @param days 近N日
     * @return
     */
    List<Xnhk0127DTO> getMissedDividendRecords(Integer days);

    /**
     * 获取近1日创建的数据异常的除权记录(CD,SD)
     * F003D为空
     * @return
     */
    List<Xnhk0127DTO> getErrorDividendRecords();
}
