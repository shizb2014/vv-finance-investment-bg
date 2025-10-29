package com.vv.finance.investment.bg.dto.uts.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author:maling
 * @Date:2023/7/17
 * @Description:
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendAmountInfoResp implements Serializable {
    private static final long serialVersionUID = 2256933158443491941L;

    /**
     * 股票编码
     */
    private String stockCode;

    /**
     * 每股派息
     */
    private BigDecimal dividendAmount;


}