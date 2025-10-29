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
@ApiModel(value="Xnhks0104对象", description="")
public class Xnhks0104 implements Serializable {

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

    @TableField("F006V")
    private String f006v;

    @TableField("F007V")
    private String f007v;

    @TableField("F008V")
    private String f008v;

    @TableField("F009V")
    private String f009v;

    @TableField("F010V")
    private String f010v;

    @TableField("F011V")
    private String f011v;

    @TableField("F012V")
    private String f012v;

    @TableField("F013V")
    private String f013v;

    @TableField("F014V")
    private String f014v;

    @TableField("F015V")
    private String f015v;

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

    public static final String F006V = "F006V";

    public static final String F007V = "F007V";

    public static final String F008V = "F008V";

    public static final String F009V = "F009V";

    public static final String F010V = "F010V";

    public static final String F011V = "F011V";

    public static final String F012V = "F012V";

    public static final String F013V = "F013V";

    public static final String F014V = "F014V";

    public static final String F015V = "F015V";

    public static final String CREATE_DATE = "Create_Date";

    public static final String MODIFIED_DATE = "Modified_Date";

    public static final String XDBMASK = "XDBMASK";

}
