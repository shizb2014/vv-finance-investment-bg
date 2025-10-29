package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * @ClassName TradeCalandar
 * @Deacription 交易日历（公司维护）
 * @Author wangyan
 * @Date 2023年09月15日
 **/
@Data
@TableName(value = "t_trade_calandar")
public class TradeCalendar implements Serializable {

    private static final long serialVersionUID = -4211251252934505904L;

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

}
