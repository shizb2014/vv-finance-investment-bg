package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * xnhk0211
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0211")
public class Xnhk0211 implements Serializable {
    private String seccode;

    private Long f001d;

    private String f005v;

    private String f002v;

    private BigDecimal f003n;

    private String f004v;

    private Long f006d;

    private BigDecimal f007n;

    private BigDecimal f008n;

    private BigDecimal f009n;

    private BigDecimal f010n;

    private BigDecimal f011n;

    private BigDecimal f012n;

    private BigDecimal f013n;

    private BigDecimal f014n;

    private Date createDate;

    private Date modifiedDate;

    private Long xdbmask;

    private static final long serialVersionUID = 1L;
}