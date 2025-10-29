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
 * @Date: 2021/8/16 11:46
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0311")
public class Xnhk0311 implements Serializable {

    private static final long serialVersionUID = 1867166061075798360L;

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

    @TableField("F005N")
    @Column(name = "F005N")
    private BigDecimal f005n;

    @TableField("F006N")
    @Column(name = "F006N")
    private BigDecimal f006n;

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
