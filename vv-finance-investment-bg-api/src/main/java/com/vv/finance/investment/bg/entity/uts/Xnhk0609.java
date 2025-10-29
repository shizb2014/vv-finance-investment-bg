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
@TableName("XNHK0609")
@ApiModel(value = "Xnhk0609", description = "")
public class Xnhk0609 implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

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

    @TableField("Create_Date")
    private Date createDate;
    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;
}
