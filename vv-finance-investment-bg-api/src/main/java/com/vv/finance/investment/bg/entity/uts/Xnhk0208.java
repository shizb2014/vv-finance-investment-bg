package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("XNHK0208")
public class Xnhk0208 implements Serializable {
    private static final long serialVersionUID = 1968536166361748322L;
    @TableField("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002D")
    private Long f002d;

    @TableField("F003V")
    private String f003v;

    @TableField("F004N")
    private BigDecimal f004n;

    @TableField("F005V")
    private String f005v;

    @TableField("F006V")
    private String f006v;

    @TableField("F007N")
    private Long f007n;

    @TableField("F008N")
    private BigDecimal f008n;

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

    @TableField("F016N")
    private BigDecimal f016n;

    @TableField("F017N")
    private BigDecimal f017n;

    @TableField("F018N")
    private BigDecimal f018n;

    @TableField("F019N")
    private BigDecimal f019n;

    @TableField("F020N")
    private BigDecimal f020n;

    @TableField("F021N")
    private BigDecimal f021n;

    @TableField("F022N")
    private BigDecimal f022n;

    @TableField("F023N")
    private BigDecimal f023n;

    @TableField("F024N")
    private BigDecimal f024n;

    @TableField("F025N")
    private BigDecimal f025n;

    @TableField("F026N")
    private BigDecimal f026n;

    @TableField("F027N")
    private BigDecimal f027n;

    @TableField("F028N")
    private BigDecimal f028n;

    @TableField("F029N")
    private BigDecimal f029n;

    @TableField("F030N")
    private BigDecimal f030n;

    @TableField("F031N")
    private BigDecimal f031n;

    @TableField("F032N")
    private BigDecimal f032n;

    @TableField("F033N")
    private BigDecimal f033n;

    @TableField("F034N")
    private BigDecimal f034n;

    @TableField("F035N")
    private BigDecimal f035n;

    @TableField("F036N")
    private BigDecimal f036n;

    @TableField("F037N")
    private BigDecimal f037n;

    @TableField("F038N")
    private BigDecimal f038n;

    @TableField("F039N")
    private BigDecimal f039n;

    @TableField("F040N")
    private BigDecimal f040n;

    @TableField("F041N")
    private BigDecimal f041n;

    @TableField("F042N")
    private BigDecimal f042n;

    @TableField("F043N")
    private BigDecimal f043n;

    @TableField("F044N")
    private BigDecimal f044n;

    @TableField("F045N")
    private BigDecimal f045n;

    @TableField("F046N")
    private BigDecimal f046n;

    @TableField("F047N")
    private BigDecimal f047n;

    @TableField("F048N")
    private BigDecimal f048n;

    @TableField("F049N")
    private BigDecimal f049n;

    @TableField("F050N")
    private BigDecimal f050n;

    @TableField("F051N")
    private BigDecimal f051n;

    @TableField("F052N")
    private BigDecimal f052n;

    @TableField("F053N")
    private BigDecimal f053n;

    @TableField("F054N")
    private BigDecimal f054n;

    @TableField("F055N")
    private BigDecimal f055n;

    @TableField("F056N")
    private BigDecimal f056n;

    @TableField("F057N")
    private BigDecimal f057n;

    @TableField("F058N")
    private BigDecimal f058n;

    @TableField("F059N")
    private BigDecimal f059n;

    @TableField("F060N")
    private BigDecimal f060n;

    @TableField("F061V")
    private String f061v;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;
}