package com.vv.finance.investment.bg.entity.f10;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName NoFinOperatingProfit
 * @Deacription 经营溢利
 * @Author lh.sz
 * @Date 2021年07月20日 16:25
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class NoFinOperatingProfit extends F10Val implements Serializable {

    private static final long serialVersionUID = 5324929052155803759L;

    /**
     * 财务费用
     */
    private F10Val financingCost;
    /**
     * 共同控制及联营公司溢利
     */
    private F10Val profitFromJointControlAndAssociatedCompanies;


}
