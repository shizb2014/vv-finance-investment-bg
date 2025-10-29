package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
*   @ClassName:    HkexTd
*   @Description: 交易日历
*   @Author:   Demon
*   @Datetime:    2020/12/22   10:43
*/
@Data
@TableName(value = "HKEX_TD")
public class HkexTd implements Serializable {
    @TableId(value = "RDATE", type = IdType.INPUT)
    private Long rdate;

    @TableField(value = "ISTRADE")
    private String istrade;

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;

    @TableField(value = "XDBMASK")
    private Long xdbmask;

    private static final long serialVersionUID = 1L;

    public static final String COL_RDATE = "RDATE";

    public static final String COL_ISTRADE = "ISTRADE";

    public static final String COL_CREATE_DATE = "Create_Date";

    public static final String COL_MODIFIED_DATE = "Modified_Date";

    public static final String COL_XDBMASK = "XDBMASK";
}