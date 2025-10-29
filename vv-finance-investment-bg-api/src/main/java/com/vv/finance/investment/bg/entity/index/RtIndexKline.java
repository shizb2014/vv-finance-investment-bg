package com.vv.finance.investment.bg.entity.index;

import lombok.Data;

/**
 * @author chenyu
 * @date 2020/11/11 11:26
 */
@Data
public class RtIndexKline extends IndexBaseMinKline{
    private static final long serialVersionUID = 5427823517565686865L;
    private String avg_price;
    private String price;
}
