package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author chenzhenlong
 * @since 2021-08-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0601")
@ApiModel(value = "Xnhks0601", description = "")
public class Xnhks0601 implements Serializable {

    private static final long serialVersionUID = -5164875983567086739L;

    @TableField("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002D")
    private Long f002d;

    @TableId("F003V")
    private String f003v;

    @TableField("F004N")
    private Long f004n;

    @TableField("F005V")
    private String f005v;

    @TableField("F006V")
    private String f006v;

    @TableField("F007N")
    private BigDecimal f007n;

    @TableField("F008V")
    private String f008v;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010N")
    private BigDecimal f010n;

    @TableField("F011N")
    private BigDecimal f011n;

    @TableField("F012V")
    private String f012v;

    @TableField("F013N")
    private BigDecimal f013n;

    @TableField("F014N")
    private BigDecimal f014n;

    @TableField("F015N")
    private BigDecimal f015n;

    @TableField("F016N")
    private BigDecimal f016n;

    @TableField("F017V")
    private String f017v;

    @TableField("F018N")
    private BigDecimal f018n;

    @TableField("F019V")
    private String f019v;

    @TableField("F020N")
    private BigDecimal f020n;

    @TableField("F021N")
    private BigDecimal f021n;

    @TableField("F022N")
    private BigDecimal f022n;

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

    @TableField("F029N")
    private BigDecimal f029n;

    @TableField("F030V")
    private String f030v;

    @TableField("F031N")
    private BigDecimal f031n;

    @TableField("F032N")
    private BigDecimal f032n;

    @TableField("F033N")
    private BigDecimal f033n;

    @TableField("F034V")
    private String f034v;

    @TableField("F035N")
    private BigDecimal f035n;

    @TableField("F036N")
    private BigDecimal f036n;

    @TableField("F037N")
    private BigDecimal f037n;

    @TableField("F038N")
    private BigDecimal f038n;

    @TableField("F039N")
    private String f039n;

    @TableField("F040N")
    private String f040n;

    @TableField("F041N")
    private String f041n;

    @TableField("F042V")
    private String f042v;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
