package com.vv.finance.investment.bg.stock.quotes;

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
import java.util.Date;

/**
 * <p>
 * 指数行情快照
 * </p>
 *
 * @author hqj
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_index_snapshot")
@ApiModel(value="IndexSnapshot对象", description="指数行情快照")
public class IndexSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "协议类型")
    private String protocol;

    @ApiModelProperty(value = "指数代码")
    private String code;

    @ApiModelProperty(value = "行情时间")
    private Date time;

    @ApiModelProperty(value = "指数状态")
    private String indexstatus;

    @ApiModelProperty(value = "昨收价")
    private BigDecimal preclose;

    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;

    @ApiModelProperty(value = "最高价")
    private BigDecimal high;

    @ApiModelProperty(value = "最低价")
    private BigDecimal low;

    @ApiModelProperty(value = "收市价")
    private BigDecimal close;

    @ApiModelProperty(value = "最新价")
    private BigDecimal last;

    @ApiModelProperty(value = "成交量")
    private Integer totalvol;

    @ApiModelProperty(value = "成交额")
    private BigDecimal turnover;

    @ApiModelProperty(value = "涨跌")
    private BigDecimal netchgpreday;

    @ApiModelProperty(value = "涨跌%")
    private BigDecimal netchgpredaypct;

    @ApiModelProperty(value = "预估结算值")
    private BigDecimal easvalue;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
