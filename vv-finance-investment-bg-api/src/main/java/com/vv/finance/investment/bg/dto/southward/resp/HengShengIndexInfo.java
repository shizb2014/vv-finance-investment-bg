package com.vv.finance.investment.bg.dto.southward.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author:maling
 * @Date:2023/8/28
 * @Description:
 */
@Data
public class HengShengIndexInfo implements Serializable {
    private static final long serialVersionUID = 5963110190769410294L;

    /**
     * 日期
     */
    private LocalDate localDate;

    /**
     * 恒生指数
     */
    private BigDecimal hengshengIndex;
}