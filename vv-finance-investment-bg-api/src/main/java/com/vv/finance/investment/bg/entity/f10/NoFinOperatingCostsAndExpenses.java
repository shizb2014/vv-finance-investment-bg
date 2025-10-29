package com.vv.finance.investment.bg.entity.f10;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName OperatingCostsAndExpenses
 * @Deacription 营业及其支出
 * @Author lh.sz
 * @Date 2021年07月20日 16:25
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class NoFinOperatingCostsAndExpenses extends F10Val implements Serializable {

    private static final long serialVersionUID = 5324929052155803759L;

    /**
     * 毛利
     */
    private F10Val grossProfit;
    /**
     * 销售成本
     */
    private F10Val sellingCost;
    /**
     * 销售费用
     */
    private F10Val sellingExpense;
    /**
     * 行政费用
     */
    private F10Val administrationExpense;
    /**
     * 研发费用
     */
    private F10Val rdExpense;
    /**
     * 其他营业支出
     */
    private F10Val otherOperatingExpenditure;
    /**
     * 营业总成本
     */
    private F10Val totalOperatingExpenses;


}
