package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 11:44
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0310")
public class Xnhks0310 implements Serializable {

    private static final long serialVersionUID = -1395625993011134114L;

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002V")
    @Column(name = "F002V")
    private String f002v;

    @TableField("F003D")
    @Column(name = "F003D")
    private Long f003d;

    @TableField("F004D")
    @Column(name = "F004D")
    private Long f004d;

    @TableField("F005V")
    @Column(name = "F005V")
    private String f005v;

    @TableField("F006V")
    @Column(name = "F006V")
    private String f006v;

    @TableField("F007V")
    @Column(name = "F007V")
    private String f007v;

    @TableField("F008V")
    @Column(name = "F008V")
    private String f008v;

    @TableField("Create_Date")
    @Column(name = "Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    @Column(name = "Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    @Column(name = "XDBMASK")
    private Long xdbmask;

    @TableField("F009D")
    @Column(name = "F009D")
    private Long f009d;

    @TableField("F010D")
    @Column(name = "F010D")
    private Long f010d;

    @TableField("F011D")
    @Column(name = "F011D")
    private Long f011d;
}
