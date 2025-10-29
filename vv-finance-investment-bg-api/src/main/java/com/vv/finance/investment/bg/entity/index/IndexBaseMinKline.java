package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName: IndexBaseMinKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   14:50
 */
@Data
public class IndexBaseMinKline implements Serializable {
    private static final long serialVersionUID = 3589044313740200708L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指数代码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 时间
     */
    @TableField(value = "time")
    private Date time;

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
     * 收盘价
     */
    @TableField(value = "close")
    private BigDecimal close;

    /**
     * 昨收价
     */
    @TableField(value = "pre_close")
    private BigDecimal preClose;

    /**
     * 成交量
     */
    @TableField(value = "volume")
    private Long volume;

    /**
     * 成交额
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 涨跌额
     */
    @TableField(value = "chg")
    private BigDecimal chg;

    /**
     * 涨跌幅
     */
    @TableField(value = "chg_pct")
    private BigDecimal chgPct;

    /**
     * 复权类型
     */
    @TableField(value = "adjhkt")
    private String adjhkt;

    /**
     * 均线A
     */
    @TableField(value = "ma_5")
    private BigDecimal ma5;

    /**
     * 均线B
     */
    @TableField(value = "ma_10")
    private BigDecimal ma10;

    /**
     * 均线C
     */
    @TableField(value = "ma_20")
    private BigDecimal ma20;

    /**
     * 均线D
     */
    @TableField(value = "ma_30")
    private BigDecimal ma30;

    /**
     * 均线E
     */
    @TableField(value = "ma_50")
    private BigDecimal ma50;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    public static final String COL_ID = "id";

    public static final String COL_CODE = "code";

    public static final String COL_TIME = "time";

    public static final String COL_OPEN = "open";

    public static final String COL_HIGH = "high";

    public static final String COL_LOW = "low";

    public static final String COL_CLOSE = "close";

    public static final String COL_PRE_CLOSE = "pre_close";

    public static final String COL_VOLUME = "volume";

    public static final String COL_AMOUNT = "amount";

    public static final String COL_CHG = "chg";

    public static final String COL_CHG_PCT = "chg_pct";

    public static final String COL_ADJHKT = "adjhkt";

    public static final String COL_MA_5 = "ma_5";

    public static final String COL_MA_10 = "ma_10";

    public static final String COL_MA_20 = "ma_20";

    public static final String COL_MA_30 = "ma_30";

    public static final String COL_MA_50 = "ma_50";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}
