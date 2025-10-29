package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2021-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhks0502对象", description="")
public class Xnhks0502 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001V")
    private String f001v;

    @TableField("F002D")
    private Long f002d;

    @TableField("F003D")
    private Long f003d;

    @TableField("F004V")
    private String f004v;

    @TableField("F005D")
    private Long f005d;

    @TableField("F006D")
    private Long f006d;

    @TableField("F007D")
    private Long f007d;

    @TableField("F008D")
    private Long f008d;

    @TableField("F009D")
    private Long f009d;

    @TableField("F010D")
    private Long f010d;

    @TableField("F011D")
    private Long f011d;

    @TableField("F012D")
    private Long f012d;

    @TableField("F013D")
    private Long f013d;

    @TableField("F014D")
    private Long f014d;

    @TableField("F015D")
    private Long f015d;

    @TableField("F016D")
    private Long f016d;

    @TableField("F017D")
    private Long f017d;

    @TableField("F018D")
    private Long f018d;

    @TableField("F019D")
    private Long f019d;

    @TableField("F020D")
    private Long f020d;

    @TableField("F021D")
    private Long f021d;

    @TableField("F022D")
    private Long f022d;

    @TableField("F023D")
    private Long f023d;

    @TableField("F024D")
    private Long f024d;

    @TableField("F025D")
    private Long f025d;

    @TableField("F026V")
    private String f026v;

    @TableField("F027D")
    private Long f027d;

    @TableField("F028V")
    private String f028v;

    @TableField("F029D")
    private Long f029d;

    @TableField("F030V")
    private String f030v;

    @TableField("F031D")
    private Long f031d;

    @TableField("F032V")
    private String f032v;

    @TableField("F033D")
    private Long f033d;

    @TableField("F034V")
    private String f034v;

    @TableField("F035D")
    private Long f035d;

    @TableField("F036V")
    private String f036v;

    @TableField("F037D")
    private Long f037d;

    @TableField("F038V")
    private String f038v;

    @TableField("F039D")
    private Long f039d;

    @TableField("F040V")
    private String f040v;

    @TableField("F041D")
    private Long f041d;

    @TableField("F042V")
    private String f042v;

    @TableField("F043D")
    private Long f043d;

    @TableField("F044V")
    private String f044v;

    @TableField("F045D")
    private Long f045d;

    @TableField("F046V")
    private String f046v;

    @TableField("F047D")
    private Long f047d;

    @TableField("F048V")
    private String f048v;

    @TableField("F049D")
    private Long f049d;

    @TableField("F050V")
    private String f050v;

    @TableField("F051D")
    private Long f051d;

    @TableField("F052V")
    private String f052v;

    @TableField("F053D")
    private Long f053d;

    @TableField("F054V")
    private String f054v;

    @TableField("F055D")
    private Long f055d;

    @TableField("F056V")
    private String f056v;

    @TableField("F057D")
    private Long f057d;

    @TableField("F058V")
    private String f058v;

    @TableField("F059D")
    private Long f059d;

    @TableField("F060V")
    private String f060v;

    @TableField("F061D")
    private Long f061d;

    @TableField("F062V")
    private String f062v;

    @TableField("F063D")
    private Long f063d;

    @TableField("F064V")
    private String f064v;

    @TableField("F065D")
    private Long f065d;

    @TableField("F066V")
    private String f066v;

    @TableField("F067D")
    private Long f067d;

    @TableField("F068D")
    private Long f068d;

    @TableField("F069D")
    private Long f069d;

    @TableField("F070D")
    private Long f070d;

    @TableField("Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
    private LocalDateTime modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
