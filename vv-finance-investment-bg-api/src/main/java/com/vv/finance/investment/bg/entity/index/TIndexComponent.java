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
*   @ClassName:    TIndexComponent
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/29   14:14
*/

/**
 * 指数成分股
 */
@Data
@TableName(value = "t_index_component")
public class TIndexComponent implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 股票代码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 指数代码
     */
    @TableField(value = "index_code")
    private String indexCode;

    /**
     * 股票名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 股票价格 元
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 总成交量
     */
    @TableField(value = "volume")
    private Long volume;

    /**
     * 行情时间
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 涨跌金额
     */
    @TableField(value = "chg")
    private BigDecimal chg;

    /**
     * 涨跌幅
     */
    @TableField(value = "chg_pct")
    private BigDecimal chgPct;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_CODE = "code";

    public static final String COL_INDEX_CODE = "index_code";

    public static final String COL_NAME = "name";

    public static final String COL_PRICE = "price";

    public static final String COL_VOLUME = "volume";

    public static final String COL_AMOUNT = "amount";

    public static final String COL_CHG = "chg";

    public static final String COL_CHG_PCT = "chg_pct";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}