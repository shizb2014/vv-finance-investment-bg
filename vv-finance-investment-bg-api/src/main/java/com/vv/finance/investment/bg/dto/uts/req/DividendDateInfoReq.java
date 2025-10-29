package com.vv.finance.investment.bg.dto.uts.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/7/17
 * @Description:派息日期信息查询对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendDateInfoReq implements Serializable {

    private static final long serialVersionUID = -1370273527795862393L;

    /**
     * 股票编码列表
     */
    private List<String> stockCodeList;

    /**
     * 开始日期 格式：20220620
     */
    private Long startDate;

    /**
     * 结束日期  格式：20221021
     */
    private Long endDate;
}