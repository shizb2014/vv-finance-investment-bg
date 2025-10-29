package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 11:47
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0314")
public class Xnhks0314 implements Serializable {

    private static final long serialVersionUID = -3250601112258725401L;

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002V")
    @Column(name = "F002V")
    private String f002v;

    @TableField("F003V")
    @Column(name = "F003V")
    private String f003v;

    @TableField("F004N")
    @Column(name = "F004N")
    private BigDecimal f004n;

    @TableField("F005V")
    @Column(name = "F005V")
    private String f005v;

    @TableField("F006D")
    @Column(name = "F006D")
    private Long f006d;

    @TableField("F007D")
    @Column(name = "F007D")
    private Long f007d;

    @TableField("F008V")
    @Column(name = "F008V")
    private String f008v;

    @TableField("F009V")
    @Column(name = "F009V")
    private String f009v;

    @TableField("F010V")
    @Column(name = "F010V")
    private String f010v;

    @TableField("Create_Date")
    @Column(name = "Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    @Column(name = "Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    @Column(name = "XDBMASK")
    private Long xdbmask;
}
