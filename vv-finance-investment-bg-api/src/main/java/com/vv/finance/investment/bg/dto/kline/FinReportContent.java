package com.vv.finance.investment.bg.dto.kline;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/11/24 16:58
 */
@Data
public class FinReportContent implements Serializable {

    private static final long serialVersionUID = -7562930326398023499L;

    /**
     * 营业收入
     */
    private BigDecimal operatingRevenue;
    /**
     * 净利润
     */
    private BigDecimal netProfits;
}
