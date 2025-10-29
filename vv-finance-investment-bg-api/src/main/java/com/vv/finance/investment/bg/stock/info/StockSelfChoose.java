package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * @author chenyu
 * @date 2020/10/27 17:50
 */
@Data
@TableName("t_user_stock_correlate")
public class StockSelfChoose extends BaseEntity {
    @TableField("stock_code")
    private String stockCode;

    @TableField("stock_name")
    private String stockName;

    @TableField("user_id")
    private Long userId;

    @TableField("type")
    private Integer type;

    @JsonIgnore
    @TableField("create_by")
    private String createBy;

    @JsonIgnore
    @TableField("update_by")
    private String updateBy;

    @JsonIgnore
    @TableField("deleted_flag")
    @TableLogic
    private Integer deletedFlag;
}
