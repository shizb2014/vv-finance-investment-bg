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
 * @author chenyu
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhke0101对象", description="")
public class Xnhke0101 implements Serializable {

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

    @TableField("F024V")
    private String f024v;

    @TableField("F025V")
    private String f025v;

    @TableField("F026V")
    private String f026v;

    @TableField("F027V")
    private String f027v;

    @TableField("F028D")
    private Long f028d;

    @TableField("F029V")
    private String f029v;

    @TableField("F030V")
    private String f030v;

    @TableField("F031V")
    private String f031v;

    @TableField("F032V")
    private String f032v;

    @TableField("Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
    private LocalDateTime modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


    public static final String SECCODE = "SECCODE";

    public static final String F001V = "F001V";

    public static final String F002V = "F002V";

    public static final String F003V = "F003V";

    public static final String F004V = "F004V";

    public static final String F005V = "F005V";

    public static final String F006D = "F006D";

    public static final String F007D = "F007D";

    public static final String F008D = "F008D";

    public static final String F009D = "F009D";

    public static final String F010V = "F010V";

    public static final String F011V = "F011V";

    public static final String F012N = "F012N";

    public static final String F013V = "F013V";

    public static final String F014V = "F014V";

    public static final String F015V = "F015V";

    public static final String F016V = "F016V";

    public static final String F017V = "F017V";

    public static final String F018V = "F018V";

    public static final String F019V = "F019V";

    public static final String F020V = "F020V";

    public static final String F021V = "F021V";

    public static final String F022V = "F022V";

    public static final String F023V = "F023V";

    public static final String F024V = "F024V";

    public static final String F025V = "F025V";

    public static final String F026V = "F026V";

    public static final String F027V = "F027V";

    public static final String F028D = "F028D";

    public static final String F029V = "F029V";

    public static final String F030V = "F030V";

    public static final String F031V = "F031V";

    public static final String F032V = "F032V";

    public static final String CREATE_DATE = "Create_Date";

    public static final String MODIFIED_DATE = "Modified_Date";

    public static final String XDBMASK = "XDBMASK";

}
