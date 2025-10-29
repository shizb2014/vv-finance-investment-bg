package com.vv.finance.investment.bg.entity.f10.financial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/20 9:55
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerShareIndicatorFinancial implements Serializable {
    private static final long serialVersionUID = 6404307627598895194L;
    /**
     * 每股盈利
     */
    private F10Val earningPerShare;
    /**
     * 每股资产净值
     */
    private F10Val netAssetPerShare;

    /**
     * 每股现金流量
     */
    private F10Val cashFlowPerShare;
    

}
