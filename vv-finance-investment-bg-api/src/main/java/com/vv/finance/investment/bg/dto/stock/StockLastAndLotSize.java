package com.vv.finance.investment.bg.dto.stock;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StockLastAndLotSize
 * @Deacription 股票最新价每手股数
 * @Author lh.sz
 * @Date 2021年10月12日 11:03
 **/
@Data
@ToString
@Builder
public class StockLastAndLotSize implements Serializable {
    private static final long serialVersionUID = 9004077745145012001L;

    /**
     * 最新价
     */
    private BigDecimal last;
    /**
     * 每手股数
     */
    private Integer lotSize;
}
