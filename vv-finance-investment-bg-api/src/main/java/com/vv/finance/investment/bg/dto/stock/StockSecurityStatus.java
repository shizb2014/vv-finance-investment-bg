package com.vv.finance.investment.bg.dto.stock;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName StockSecurityStatus
 * @Deacription 股票状态
 * @Author lh.sz
 * @Date 2021年10月14日 19:14
 **/
@Data
@ToString
public class StockSecurityStatus implements Serializable {
    private static final long serialVersionUID = 9004077745145012001L;
    /**
     * 股票
     */
    private String stockCode;
    /**
     * 证券状态 1:交易中 2:停牌
     */
    private int suspension;
}
