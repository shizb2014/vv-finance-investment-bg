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
 * @ClassName: TIndexInfo
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/29   14:07
 */
@Data
@TableName(value = "t_index_info")
public class TIndexInfo implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 指数代码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 指数名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 时间
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 当前价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 开盘价格
     */
    @TableField(value = "open")
    private BigDecimal open;

    /**
     * 收盘价
     */
    @TableField(value = "yclose")
    private BigDecimal yclose;

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
     * 成交量
     */
    @TableField(value = "vol")
    private Long vol;

    /**
     * 成交额
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 振幅
     */
    @TableField(value = "amplitude")
    private BigDecimal amplitude;

    /**
     * 涨跌幅
     */
    @TableField(value = "increase")
    private BigDecimal increase;

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

    public static final String COL_NAME = "name";

    public static final String COL_DATE = "date";

    public static final String COL_PRICE = "price";

    public static final String COL_OPEN = "open";

    public static final String COL_YCLOSE = "yclose";

    public static final String COL_HIGH = "high";

    public static final String COL_LOW = "low";

    public static final String COL_VOL = "vol";

    public static final String COL_AMOUNT = "amount";

    public static final String COL_AMPLITUDE = "amplitude";

    public static final String COL_INCREASE = "increase";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}