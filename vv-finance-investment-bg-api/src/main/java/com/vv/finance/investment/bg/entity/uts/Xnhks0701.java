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
public class Xnhks0701 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001V")
    private String f001v;

    @TableField("F002V")
    private String f002v;

    @TableField("F003V")
    private String f003v;

    @TableField("F004V")
    private String f004v;

    @TableField("F005V")
    private String f005v;

    @TableField("F006D")
    private Long f006d;

    @TableField("F007D")
    private Long f007d;

    @TableField("F008D")
    private Long f008d;

    @TableField("F009D")
    private Long f009d;

    @TableField("F010V")
    private String f010v;

    @TableField("F011V")
    private String f011v;

    @TableField("F012N")
    private Long f012n;

    @TableField("F013V")
    private String f013v;

    @TableField("F014V")
    private String f014v;

    @TableField("F015V")
    private String f015v;

    @TableField("F016V")
    private String f016v;

    @TableField("F017V")
    private String f017v;

    @TableField("F018V")
    private String f018v;

    @TableField("F019V")
    private String f019v;

    @TableField("F020V")
    private String f020v;

    @TableField("F021V")
    private String f021v;

    @TableField("F022V")
    private String f022v;

    @TableField("F023V")
    private String f023v;

    @TableField("F024N")
    private BigDecimal f024n;

    @TableField("F025N")
    private BigDecimal f025n;

    @TableField("F026N")
    private BigDecimal f026n;

    @TableField("F027N")
    private BigDecimal f027n;

    @TableField("F028V")
    private String f028v;

    @TableField("F029D")
    private Long f029d;

    @TableField("F030N")
    private BigDecimal f030n;

    @TableField("F031V")
    private String f031v;

    @TableField("F032N")
    private BigDecimal f032n;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
