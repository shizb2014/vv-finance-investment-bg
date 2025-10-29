package com.vv.finance.investment.bg.entity.f10.estimation;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName RadarVal
 * @Deacription 雷达图数据
 * @Author lh.sz
 * @Date 2021年09月07日 10:13
 **/
@Data
@ToString
@Builder
public class RadarVal implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    private BigDecimal profit;

    private BigDecimal growth;

    private BigDecimal operating;

    private BigDecimal debt;

    private BigDecimal cash;
}
