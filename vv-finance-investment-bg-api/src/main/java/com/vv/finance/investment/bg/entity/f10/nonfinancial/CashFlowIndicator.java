package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashFlowIndicator implements Serializable {
    private static final long serialVersionUID = 7639518950378814472L;

    /**
     * 销售现金比率
     */
    private F10Val salesCashRatio;
    /**
     * 经营现金净流量/利润总额
     */
    private F10Val netOperatingCashFlowAndTotalProfit;
    /**
     * 经营现金净流量/营业总收入
     */
    private F10Val netOperatingCashFlowAndGrossOperatingIncome;
    /**
     * 净利润现金含量
     */
    private F10Val netProfitCashContent;
    /**
     * 营收收入现金含量
     */
    private F10Val cashContentRevenueIncome;
}
