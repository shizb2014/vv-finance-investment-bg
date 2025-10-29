package com.vv.finance.investment.bg.entity.f10.nonfinancial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.*;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 18:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatingCapacityNonFinancial implements Serializable {
    private static final long serialVersionUID = -8049429227414875336L;
    /**
     * 资产周转率
     */
    private F10Val totalAssetsTurnover;
    /**
     * 流动资金周转率
     */
    private F10Val currentAssetsTurnover;
    /**
     * 存货转换周期
     */
    private F10Val inventoryTurnover;
    /**
     * 应收账转换周期
     */
    private F10Val accountsReceivableTurnover;

    /**
     * 应付账转换周期
     */
    private F10Val accountsPayableTurnover;

    /**
     * 股东权益周转率
     */
    private F10Val tose;
    /**
     * 存货周转率
     */
    private F10Val itr;
    /**
     * 应收款周转率
     */
    private F10Val toar;
    /**
     * 应付款周转率
     */
    private F10Val troap;
}
