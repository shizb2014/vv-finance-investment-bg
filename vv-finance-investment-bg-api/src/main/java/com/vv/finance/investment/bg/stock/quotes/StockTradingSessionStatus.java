//package com.vv.finance.investment.bg.stock.quotes;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.stockTime.LocalDateTime;
//
///**
// * <p>
// * 交易时段状态
// * </p>
// *
// * @author hqj
// * @since 2020-10-23
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//@TableName("t_stock_trading_session_status")
//@ApiModel(value = "StockTradingSessionStatus对象", description = "交易时段状态")
//public class StockTradingSessionStatus implements Serializable {
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
//    @ApiModelProperty(value = "交易时段  ID")
//    private Integer subid;
//
//    @ApiModelProperty(value = "交易时段状态")
//    private BigDecimal status;
//
//    @ApiModelProperty(value = "交易时段控制	标识")
//    private Integer controlflag;
//
//    @ApiModelProperty(value = "开始时间")
//    private Integer starttime;
//
//    @ApiModelProperty(value = "结束时间")
//    private Integer endtime;
//
//    @ApiModelProperty(value = "创建时间")
//    private LocalDateTime createTime;
//
//    @ApiModelProperty(value = "更新时间")
//    private LocalDateTime updateTime;
//
//
//}
