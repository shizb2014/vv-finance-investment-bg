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
 * @ClassName: TIndexMinChart
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/29   18:52
 */

/**
 * 指数分时图
 */
@Data
@TableName(value = "t_index_min_chart")
public class TIndexMinChart implements Serializable {
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
     * 最新价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 平均价格
     */
    @TableField(value = "avg_price")
    private BigDecimal avgPrice;

    /**
     * 成交量
     */
    @TableField(value = "volume")
    private Long volume;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_CODE = "code";

    public static final String COL_TIME = "time";

    public static final String COL_PRICE = "price";

    public static final String COL_AVG_PRICE = "avg_price";

    public static final String COL_VOLUME = "volume";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}