package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName: TIndexSnapshot
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/29   14:28
 */

/**
 * 指数行情快照
 */
@Data
@TableName(value = "t_index_snapshot")
public class TIndexSnapshot implements Serializable {
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
     * 行情时间
     */
    @TableField(value = "time")
    private Date time;

    /**
     * 指数状态(67-关闭，84-正常)
     */
    @TableField(value = "indexstatus")
    private String indexstatus;

    /**
     * 昨收价
     */
    @TableField(value = "preclose")
    private BigDecimal preclose;

    /**
     * 开盘价
     */
    @TableField(value = "open")
    private BigDecimal open;

    /**
     * 最高价
     */
    @TableField(value = "high")
    private BigDecimal high;

    /**
     * 最低价
     */
    @TableField(value = "low")
    private BigDecimal low;

    /**
     * 收市价
     */
    @TableField(value = "close")
    private BigDecimal close;

    /**
     * 最新价
     */
    @TableField(value = "last")
    private BigDecimal last;

    /**
     * 成交量
     */
    @TableField(value = "totalvol")
    private Long totalvol;

    /**
     * 成交额
     */
    @TableField(value = "turnover")
    private BigDecimal turnover;

    /**
     * 涨跌
     */
    @TableField(value = "netchgpreday")
    private BigDecimal netchgpreday;

    /**
     * 涨跌%
     */
    @TableField(value = "netchgpredaypct")
    private BigDecimal netchgpredaypct;

    /**
     * 预估结算值
     */
    @TableField(value = "easvalue")
    private BigDecimal easvalue;

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

    public static final String COL_TIME = "time";

    public static final String COL_INDEXSTATUS = "indexstatus";

    public static final String COL_PRECLOSE = "preclose";

    public static final String COL_OPEN = "open";

    public static final String COL_HIGH = "high";

    public static final String COL_LOW = "low";

    public static final String COL_CLOSE = "close";

    public static final String COL_LAST = "last";

    public static final String COL_TOTALVOL = "totalvol";

    public static final String COL_TURNOVER = "turnover";

    public static final String COL_NETCHGPREDAY = "netchgpreday";

    public static final String COL_NETCHGPREDAYPCT = "netchgpredaypct";

    public static final String COL_EASVALUE = "easvalue";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}