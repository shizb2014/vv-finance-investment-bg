package com.vv.finance.investment.bg.dto.Indicator;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chenyu
 * @date 2020/10/30 17:56
 */
@Data
public class BaseIndicatorDTO implements Serializable {
    private static final long serialVersionUID = 7699622755385549541L;
    /**
     * 股票代码
     */
    private String code;

    /**
     * 日期
     */
    private Date date;
}
