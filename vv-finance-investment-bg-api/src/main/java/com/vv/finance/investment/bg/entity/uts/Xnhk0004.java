package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
*   @ClassName:    Xnhk0004
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/12/22   10:45
*/
@Data
@TableName(value = "XNHK0004")
public class Xnhk0004 implements Serializable {
    /**
     * 代码
     */
    @TableId(value = "CODE", type = IdType.INPUT)
    private String code;

    /**
     * 简体中文名称
     */
    @TableField(value = "F001V")
    private String f001v;

    /**
     * 英文名称
     */
    @TableField(value = "F002V")
    private String f002v;

    /**
     * 繁体中文名称
     */
    @TableField(value = "F003V")
    private String f003v;

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;

    /**
     * 时间戳
     */
    @TableField(value = "XDBMASK")
    private Long xdbmask;

    private static final long serialVersionUID = 1L;

    public static final String COL_CODE = "CODE";

    public static final String COL_F001V = "F001V";

    public static final String COL_F002V = "F002V";

    public static final String COL_F003V = "F003V";

    public static final String COL_CREATE_DATE = "Create_Date";

    public static final String COL_MODIFIED_DATE = "Modified_Date";

    public static final String COL_XDBMASK = "XDBMASK";
}