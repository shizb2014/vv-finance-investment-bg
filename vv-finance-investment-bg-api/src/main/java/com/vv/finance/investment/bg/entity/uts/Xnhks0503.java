package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author chenyu
 * @since 2021-03-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0503")
@ApiModel(value="Xnhks0503对象", description="")
public class Xnhks0503 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001V")
    private String f001v;

    @TableField("F002D")
    private Long f002d;

    @TableField("F003V")
    private String f003v;

    @TableField("F004V")
    private String f004v;

    @TableField("F005D")
    private Long f005d;

    @TableField("F006D")
    private Long f006d;

    @TableField("F007V")
    private String f007v;

    @TableField("F008N")
    private BigDecimal f008n;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010V")
    private String f010v;

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

    @TableField("F016V")
    private String f016v;

    @TableField("F017V")
    private String f017v;

    @TableField("F018V")
    private String f018v;

    @TableField("F019V")
    private String f019v;

    @TableField("F020N")
    private BigDecimal f020n;

    @TableField("F021N")
    private Long f021n;

    @TableField("F022V")
    private String f022v;

    @TableField("F023N")
    private BigDecimal f023n;

    @TableField("F024D")
    private Long f024d;

    @TableField("F025V")
    private String f025v;

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

    @TableField("F039V")
    private String f039v;

    @TableField("F040V")
    private String f040v;

    @TableField("F041V")
    private String f041v;

    @TableField("F042V")
    private String f042v;

    @TableField("F043V")
    private String f043v;

    @TableField("F044V")
    private String f044v;

    @TableField("F045V")
    private String f045v;

    @TableField("F046V")
    private String f046v;

    @TableField("F047V")
    private String f047v;

    @TableField("F048V")
    private String f048v;

    @TableField("F049V")
    private String f049v;

    @TableField("Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
    private LocalDateTime modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;

}
