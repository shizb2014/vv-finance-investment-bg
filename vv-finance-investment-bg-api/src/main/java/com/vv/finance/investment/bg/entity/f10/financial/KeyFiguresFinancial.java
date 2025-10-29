package com.vv.finance.investment.bg.entity.f10.financial;

import com.vv.finance.investment.bg.entity.f10.F10Val;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/7/19 16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyFiguresFinancial implements Serializable {
    private static final long serialVersionUID = 3975706599786357199L;
    /**
     * 营业收入
     */
    private F10Val operatingRevenue;
    /**
     * 净利润
     */
    private F10Val netProfits;




}
