package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
*   @ClassName:    Xnhks0501
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/12/11   16:32
*/
@Data
@TableName(value = "XNHKS0501")
public class Xnhks0501 implements Serializable {
    @TableId(value = "SECCODE")
    private String seccode;

    @TableField(value = "F002D")
    private Long f002d;

    @TableField(value = "F001V")
    private String f001v;

    @TableField(value = "F003V")
    private String f003v;

    @TableField(value = "F004V")
    private String f004v;

    @TableField(value = "F005V")
    private String f005v;

    @TableField(value = "F006V")
    private String f006v;

    @TableField(value = "F007V")
    private String f007v;

    @TableField(value = "F008V")
    private String f008v;

    @TableField(value = "F009V")
    private String f009v;

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;

    @TableField(value = "XDBMASK")
    private Long xdbmask;

    private static final long serialVersionUID = 1L;

    public static final String COL_SECCODE = "SECCODE";

    public static final String COL_F002D = "F002D";

    public static final String COL_F001V = "F001V";

    public static final String COL_F003V = "F003V";

    public static final String COL_F004V = "F004V";

    public static final String COL_F005V = "F005V";

    public static final String COL_F006V = "F006V";

    public static final String COL_F007V = "F007V";

    public static final String COL_F008V = "F008V";

    public static final String COL_F009V = "F009V";

    public static final String COL_CREATE_DATE = "Create_Date";

    public static final String COL_MODIFIED_DATE = "Modified_Date";

    public static final String COL_XDBMASK = "XDBMASK";
}