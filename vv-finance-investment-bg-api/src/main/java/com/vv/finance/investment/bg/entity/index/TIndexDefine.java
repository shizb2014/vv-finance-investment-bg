package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
*   @ClassName:    TIndexDefine
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/29   13:50
*/

/**
 * 指数码表
 */
@Data
@TableName(value = "t_index_define")
public class TIndexDefine implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指数代码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 指数来源
     */
    @TableField(value = "indexsource")
    private String indexsource;

    /**
     * 币种
     */
    @TableField(value = "currency")
    private String currency;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_CODE = "code";

    public static final String COL_INDEXSOURCE = "indexsource";

    public static final String COL_CURRENCY = "currency";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}