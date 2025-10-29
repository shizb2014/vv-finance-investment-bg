package com.vv.finance.investment.bg.entity.trade;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author wsliang
 * @date 2021/12/25 14:06
 **/
@Data
@ToString
@TableName(value = "t_trade_statistics_detail")
public class TradeStatisticsDetail implements Serializable {
    private static final long serialVersionUID = 6374942530708385125L;

    @TableId(value = "id", type = IdType.INPUT)
    private long id;

    @TableField(value = "code")
    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "时间")
    private long time;

    @ApiModelProperty("流入资金总额")
    @TableField(value = "capital_out")
    private BigDecimal capitalOut;

    @ApiModelProperty("流入资金小单")
    @TableField(value = "capital_in")
    private BigDecimal capitalIn;

    @ApiModelProperty("流入资金中单")
    @TableField(value = "capital_net")
    private BigDecimal capitalNet;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    private LocalDateTime createTime;


    public static final String COL_STOCK_CODE = "code";
    public static final String COL_TIME = "time";

    public void init(long time,String code){
        this.code = code;
        this.time = time;
        this.capitalOut = BigDecimal.ZERO;
        this.capitalIn=BigDecimal.ZERO;
        this.capitalNet=BigDecimal.ZERO;
    }
}
