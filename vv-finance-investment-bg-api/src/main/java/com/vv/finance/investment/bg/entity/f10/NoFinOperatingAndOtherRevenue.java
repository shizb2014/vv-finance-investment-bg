package com.vv.finance.investment.bg.entity.f10;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassName OperatingAndOtherRevenue
 * @Deacription 营业及其收入
 * @Author lh.sz
 * @Date 2021年07月20日 16:25
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class NoFinOperatingAndOtherRevenue extends F10Val implements Serializable {

    private static final long serialVersionUID = 5324929052155803759L;

    /**
     * 主营业务收入
     */
    private F10Val primeOperatingRevenue;
    /**
     * 其他业务收入
     */
    private F10Val otherOperatingRevenue;


}
