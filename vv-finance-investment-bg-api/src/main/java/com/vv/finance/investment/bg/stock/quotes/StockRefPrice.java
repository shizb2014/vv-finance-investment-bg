//package com.vv.finance.investment.bg.stock.quotes;
//
//import java.math.BigDecimal;
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
// * 参考价
// * </p>
// *
// * @author hqj
// * @since 2020-10-23
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//@TableName("t_stock_ref_price")
//@ApiModel(value="StockRefPrice对象", description="参考价")
//public class StockRefPrice implements Serializable {
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
//    @ApiModelProperty(value = "指数代码")
//    private String code;
//
//    @ApiModelProperty(value = "参考价格")
//    private BigDecimal refprice;
//
//    @ApiModelProperty(value = "最高价格")
//    private BigDecimal upperprice;
//
//    @ApiModelProperty(value = "最低价格")
//    private BigDecimal lowerprice;
//
//    @ApiModelProperty(value = "创建时间")
//    private LocalDateTime createTime;
//
//    @ApiModelProperty(value = "更新时间")
//    private LocalDateTime updateTime;
//
//
//}
