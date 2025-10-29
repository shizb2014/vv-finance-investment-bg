//package com.vv.finance.investment.bg.stock.quotes;
//
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import java.stockTime.LocalDateTime;
//import java.io.Serializable;
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
///**
// * <p>
// * 订单不平衡
// * </p>
// *
// * @author hqj
// * @since 2020-10-23
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//@TableName("t_stock_order_imbalance")
//@ApiModel(value="StockOrderImbalance对象", description="订单不平衡")
//public class StockOrderImbalance implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @ApiModelProperty(value = "主键")
//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;
//
//    @ApiModelProperty(value = "协议类型")
//    private String protocol;
//
//    @ApiModelProperty(value = "证券代码")
//    private String code;
//
//    @ApiModelProperty(value = "订单不平衡方向")
//    private String direction;
//
//    @ApiModelProperty(value = "订单不平衡数量")
//    private Integer quantity;
//
//    @ApiModelProperty(value = "创建时间")
//    private LocalDateTime createTime;
//
//    @ApiModelProperty(value = "更新时间")
//    private LocalDateTime updateTime;
//
//
//}
