package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author chenyu
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Xnhks0112对象", description = "")
public class Xnhks0112 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002V")
    private String f002v;

    @TableField("F003V")
    private String f003v;

    @TableField("F004D")
    private Long f004D;

    @TableField("F005V")
    private String f005v;

    @TableField("F006V")
    private String f006v;

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

    @TableField("XDBMASK")
    private Long xdbmask;
}
