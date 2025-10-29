package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhks0401对象", description="")
public class Xnhks0401 implements Serializable {

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

    @TableField("F006N")
    private BigDecimal f006n;

    @TableField("F007N")
    private BigDecimal f007n;

    @TableField("F008N")
    private Long f008n;

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

    public static final String F006N = "F006N";

    public static final String F007N = "F007N";

    public static final String F008N = "F008N";

    public static final String F009N = "F009N";

    public static final String F010N = "F010N";

    public static final String F011N = "F011N";

    public static final String F012V = "F012V";

    public static final String F013N = "F013N";

    public static final String F014V = "F014V";

    public static final String F015V = "F015V";

    public static final String F016V = "F016V";

    public static final String F017V = "F017V";

    public static final String F018V = "F018V";

    public static final String F019V = "F019V";

    public static final String CREATE_DATE = "Create_Date";

    public static final String MODIFIED_DATE = "Modified_Date";

    public static final String XDBMASK = "XDBMASK";

}
