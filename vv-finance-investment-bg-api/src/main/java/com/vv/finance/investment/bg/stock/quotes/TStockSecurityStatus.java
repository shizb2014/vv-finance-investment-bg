//package com.vv.finance.investment.bg.stock.quotes;
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableName;
//import lombok.Data;
//
///**
//*   @ClassName:    TSecurityStatus
//*   @Description:
//*   @Author:   Demon
//*   @Datetime:    2020/10/23   10:03
//*/
///**
//    * 证券状态
//    */
//@Data
//@TableName(value = "t_Stock_security_status")
//public class TStockSecurityStatus extends BaseEntity {
//
//    /**
//     * 协议类型
//     */
//    @TableField(value = "protocol")
//    private String protocol;
//
//    /**
//     * 证券代码
//     */
//    @TableField(value = "code")
//    private String code;
//
//    /**
//     * 停牌标识（2-暂停交易或者停牌，3-复牌）
//     */
//    @TableField(value = "suspension")
//    private Byte suspension;
//
//    private static final long serialVersionUID = 1L;
//
//    public static final String COL_CODE = "code";
//
//    public static final String COL_SUSPENSION = "suspension";
//
//}