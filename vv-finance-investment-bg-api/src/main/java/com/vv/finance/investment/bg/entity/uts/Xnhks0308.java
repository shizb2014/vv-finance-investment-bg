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
 * @Date: 2021/8/16 11:34
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0308")
public class Xnhks0308 implements Serializable {

    private static final long serialVersionUID = 7384511029925466617L;

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002D")
    @Column(name = "F002D")
    private Long f002d;

    @TableField("F003D")
    @Column(name = "F003D")
    private Long f003d;

    @TableField("F004D")
    @Column(name = "F004D")
    private Long f004d;

    @TableField("F005D")
    @Column(name = "F005D")
    private Long f005d;

    @TableField("F006V")
    @Column(name = "F006V")
    private String f006v;

    @TableField("Create_Date")
//    @Column(name = "Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
//    @Column(name = "Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    @Column(name = "XDBMASK")
    private Long xdbmask;
}
