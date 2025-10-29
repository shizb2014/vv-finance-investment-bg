package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author lh.sz
 * @since 2021-12-20
 */
@Data
@Accessors(chain = true)
public class Xnhk0702 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002N")
    private BigDecimal f002n;

    @TableField("F003N")
    private BigDecimal f003n;

    @TableField("F004N")
    private BigDecimal f004n;

    @TableField("F005N")
    private BigDecimal f005n;

    @TableField("F006N")
    private BigDecimal f006n;

    @TableField("F007N")
    private BigDecimal f007n;

    @TableField("F008N")
    private Long f008n;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010N")
    private BigDecimal f010n;

    @TableField("F011N")
    private BigDecimal f011n;

    @TableField("F012N")
    private BigDecimal f012n;

    @TableField("F013N")
    private BigDecimal f013n;

    @TableField("F014N")
    private BigDecimal f014n;

    @TableField("F015N")
    private BigDecimal f015n;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
