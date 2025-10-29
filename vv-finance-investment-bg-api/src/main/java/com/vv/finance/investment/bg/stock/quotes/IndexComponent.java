package com.vv.finance.investment.bg.stock.quotes;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/26 11:50
 */
@Data
@TableName("t_index_component")
public class IndexComponent extends BaseEntity {
    private static final long serialVersionUID = -7901702473262669529L;
    private String code;
    private String indexCode;
    private String name;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal amount;
    private BigDecimal chg;
    private BigDecimal chgPct;
}
