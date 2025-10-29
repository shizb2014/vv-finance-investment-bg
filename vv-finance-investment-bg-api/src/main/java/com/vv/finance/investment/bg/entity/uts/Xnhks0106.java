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
@ApiModel(value="Xnhks0106对象", description="")
public class Xnhks0106 implements Serializable {

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

    @TableField("F007N")
    private BigDecimal f007n;

    @TableField("F008D")
    private Long f008d;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010N")
    private Integer f010n;

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

    public static final String F007N = "F007N";

    public static final String F008D = "F008D";

    public static final String F009N = "F009N";

    public static final String F010N = "F010N";

    public static final String CREATE_DATE = "Create_Date";

    public static final String MODIFIED_DATE = "Modified_Date";

    public static final String XDBMASK = "XDBMASK";

}
