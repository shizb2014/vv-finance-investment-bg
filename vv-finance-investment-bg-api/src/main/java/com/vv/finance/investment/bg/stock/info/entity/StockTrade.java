package com.vv.finance.investment.bg.stock.info.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 逐笔交易
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_trade")
@ApiModel(value="StockTrade对象", description="逐笔交易")
public class StockTrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "证券代码")
    private String code;

    @ApiModelProperty(value = "证券名称")
    private String name;

    @ApiModelProperty(value = "行情时间")
    private Long time;

    @ApiModelProperty(value = "序号")
    private Long tickerid;

    @ApiModelProperty(value = "成交价")
    private BigDecimal price;

    @ApiModelProperty(value = "成交量")
    private Long qty;

    @ApiModelProperty(value = "成交类型")
    private String type;

    @ApiModelProperty(value = "撤单标识")
    private String cancelflag;

    @ApiModelProperty(value = "买卖方向（B-主动买入，S-主动卖出，其他中性盘）")
    private String direction;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "时间序号id")
    private String serialId;


}
