package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: wsliang
 * @Date: 2021/9/2 10:35
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0605")
@ApiModel(value = "Xnhk0605", description = "")
public class Xnhk0605 implements Serializable {
    private static final long serialVersionUID = -565583459775940958L;

    @TableField("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002V")
    private String f002v;
    @TableField("F003N")
    private BigDecimal f003n;
    @TableField("F004N")
    private BigDecimal f004n;
    @TableField("F005N")
    private BigDecimal f005n;
    @TableField("F006N")
    private BigDecimal f006n;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;

}
