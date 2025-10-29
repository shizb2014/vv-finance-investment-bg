package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0129")
public class Xnhk0129 implements Serializable {

    private static final long serialVersionUID = 7625198592864821946L;

    @TableField("SECCODE")
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

    @TableField("F006V")
    private String f006v;

    @TableField("F007V")
    private String f007v;

    @TableField("F008D")
    private Long f008d;

    @TableField("F009V")
    private String f009v;

    @TableField("F010V")
    private String f010v;

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

    @TableField("F026V")
    private String f026v;

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

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;

    @TableField(value = "XDBMASK")
    private Long xdbmask;
}
