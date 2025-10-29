package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 11:43
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0309")
public class Xnhks0309 implements Serializable {

    private static final long serialVersionUID = 3862406219424106948L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002D")
    private Long f002d;

    @TableField("F003D")
    private Long f003d;

    @TableField("F004V")
    private String f004v;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;
}
