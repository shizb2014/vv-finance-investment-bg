package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHK0610")
@ApiModel(value = "Xnhk0610", description = "")
public class Xnhk0610 implements Serializable {

    private static final long serialVersionUID = -461833463959847950L;

//    @TableField("SECCODE")
//    private String seccode;

    @TableField("F001V")
    private String f001v;
    @TableField("F002V")
    private String f002v;
    @TableField("F003V")
    private String f003v;
    @TableField("F004V")
    private String f004v;
    @TableField("F005V")
    private String f005v;
    @TableField("F006V")
    private String f006v;
    @TableField("F007V")
    private String f007v;
    @TableField("F008V")
    private String f008v;
    @TableField("F009V")
    private String f009v;
    @TableField("F010V")
    private String f010v;
    @TableField("F011V")
    private String f011v;

    @TableField("Create_Date")
    private Date createDate;
    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;
}
