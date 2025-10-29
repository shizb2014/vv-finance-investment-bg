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
@TableName(value = "t_trade_statistics")
public class TradeStatistics implements Serializable {
    private static final long serialVersionUID = 6374942530708385125L;

    @TableId(value = "id", type = IdType.INPUT)
    private long id;

    @TableField(value = "code")
    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "时间")
    private long time;

    @ApiModelProperty("流入资金总额")
    @TableField(value = "in_capital_total")
    private BigDecimal inCapitalTotal;

    @ApiModelProperty("流入资金小单")
    @TableField(value = "in_capital_small")
    private BigDecimal inCapitalSmall;

    @ApiModelProperty("流入资金中单")
    @TableField(value = "in_capital_mid")
    private BigDecimal inCapitalMid;

    @ApiModelProperty("流入资金大单")
    @TableField(value = "in_capital_large")
    private BigDecimal inCapitalLarge;

    @ApiModelProperty("流入资金特大单")
    @TableField(value = "in_capital_xlarge")
    private BigDecimal inCapitalXLarge;

    @ApiModelProperty("流入总单数")
    @TableField(value = "in_qty_total")
    private BigDecimal inQtyTotal;

    @ApiModelProperty("流入小单数")
    @TableField(value = "in_qty_small")
    private BigDecimal inQtySmall;

    @ApiModelProperty("流入中单数")
    @TableField(value = "in_qty_mid")
    private BigDecimal inQtyMid;

    @ApiModelProperty("流入大单数")
    @TableField(value = "in_qty_large")
    private BigDecimal inQtyLarge;

    @ApiModelProperty("流入特大单数")
    @TableField(value = "in_qty_xlarge")
    private BigDecimal inQtyXLarge;

    @ApiModelProperty("流入资金总额")
    @TableField(value = "out_capital_total")
    private BigDecimal outCapitalTotal;

    @ApiModelProperty("流入资金小单")
    @TableField(value = "out_capital_small")
    private BigDecimal outCapitalSmall;

    @ApiModelProperty("流入资金中单")
    @TableField(value = "out_capital_mid")
    private BigDecimal outCapitalMid;

    @ApiModelProperty("流入资金大单")
    @TableField(value = "out_capital_large")
    private BigDecimal outCapitalLarge;

    @ApiModelProperty("流入资金特大单")
    @TableField(value = "out_capital_xlarge")
    private BigDecimal outCapitalXLarge;

    @ApiModelProperty("流入总单数")
    @TableField(value = "out_qty_total")
    private BigDecimal outQtyTotal;

    @ApiModelProperty("流入小单数")
    @TableField(value = "out_qty_small")
    private BigDecimal outQtySmall;

    @ApiModelProperty("流入中单数")
    @TableField(value = "out_qty_mid")
    private BigDecimal outQtyMid;

    @ApiModelProperty("流入大单数")
    @TableField(value = "out_qty_large")
    private BigDecimal outQtyLarge;

    @ApiModelProperty("流入特大单数")
    @TableField(value = "out_qty_xlarge")
    private BigDecimal outQtyXLarge;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    private LocalDateTime createTime;


    public static final String COL_STOCK_CODE = "code";
    public static final String COL_TIME = "time";

    public void init(long time,String code){
        this.code = code;
        this.time = time;
        this.inCapitalTotal = BigDecimal.ZERO;
        this.inCapitalSmall=BigDecimal.ZERO;
        this.inCapitalMid=BigDecimal.ZERO;
        this.inCapitalLarge=BigDecimal.ZERO;
        this.inCapitalXLarge=BigDecimal.ZERO;
        this.inQtyTotal=BigDecimal.ZERO;
        this.inQtySmall=BigDecimal.ZERO;
        this.inQtyMid=BigDecimal.ZERO;
        this.inQtyLarge=BigDecimal.ZERO;
        this.inQtyXLarge=BigDecimal.ZERO;
        this.outCapitalTotal=BigDecimal.ZERO;
        this.outCapitalSmall=BigDecimal.ZERO;
        this.outCapitalMid=BigDecimal.ZERO;
        this.outCapitalLarge=BigDecimal.ZERO;
        this.outCapitalXLarge=BigDecimal.ZERO;
        this.outQtyTotal=BigDecimal.ZERO;
        this.outQtySmall=BigDecimal.ZERO;
        this.outQtyMid=BigDecimal.ZERO;
        this.outQtyLarge=BigDecimal.ZERO;
        this.outQtyXLarge=BigDecimal.ZERO;
    }
}
