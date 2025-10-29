package com.vv.finance.investment.bg.dto.uts.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Author:maling
 * @Date:2023/7/17
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendDateInfoResp implements Serializable {
    private static final long serialVersionUID = 1938363476355863922L;

    /**
     * 股票编码
     */
    private String stockCode;

    /**
     * 派息日期
     */
    private LocalDate dividendDate;

    /**
     * 派息备注
     */
    private String dividendRemark;
}