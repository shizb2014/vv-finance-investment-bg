package com.vv.finance.investment.bg.entity.f10.shareholder;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yangpeng
 * @date 2023/10/24 14:14
 * @description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_stock_holder_change")
@EqualsAndHashCode(callSuper = true)
public class StockHolderChange extends BaseEntity {

    private static final long serialVersionUID = -38236993767961395L;

    /**
     * 股票code
     */
    @TableField("code")
    private String code;

    /**
     * 股东类型
     */
    @TableField("holder_type")
    private String holderType;

    /**
     * 股份类型
     */
    @TableField("share_type")
    private String shareType;

    /**
     * 持股数量
     */
    @TableField("num")
    private BigDecimal num;

    /**
     * 占比
     */
    @TableField("pop")
    private BigDecimal pop;

    /**
     * 股票变更日期 yyyyMMdd
     */
    @TableField("change_date")
    private Long changeDate;

    /**
     * 所属季度 yyyyMMdd
     */
    @TableField("qua_date")
    private Long quaDate;
}

