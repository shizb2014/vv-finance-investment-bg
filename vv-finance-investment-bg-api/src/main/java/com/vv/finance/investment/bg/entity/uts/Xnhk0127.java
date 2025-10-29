package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author chenyu
 * @since 2021-02-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0127")
@Accessors(chain = true)
public class Xnhk0127 implements Serializable {
    public static final List<String> DIVIDEND_TYPE_LIST = Arrays.asList("CD","SD");
    public static final List<String> SS_SC_TYPE_LIST = Arrays.asList("SS","SC");

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    @Column(name = "ID")
    private Long id;

    @TableField("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001N")
    @Column(name = "F001N")
    private Integer f001n;

    @TableField("F002V")
    @Column(name = "F002V")
    private String f002v;

    @TableField("F003D")
    @Column(name = "F003D")
    private LocalDate f003d;

    @TableField("F004N")
    @Column(name = "F004N")
    private BigDecimal f004n;

    @TableField("F005N")
    @Column(name = "F005N")
    private BigDecimal f005n;

    @TableField("F006V")
    @Column(name = "F006V")
    private String f006v;

    @TableField("F007N")
    @Column(name = "F007N")
    private Integer f007n;

    @TableField("Create_Date")
//    @Column(name = "Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
//    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @TableField("XDBMASK")
    @Column(name = "XDBMASK")
    private Long xdbmask;


}
