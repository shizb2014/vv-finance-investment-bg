package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2021-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhk0602对象", description="")
public class Xnhk0602 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
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

    @TableField("F007N")
    private BigDecimal f007n;

    @TableField("F008N")
    private BigDecimal f008n;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010N")
    private BigDecimal f010n;

    @TableField("F011N")
    private BigDecimal f011n;

    @TableField("F012N")
    private BigDecimal f012n;

    @TableField("F013N")
    private BigDecimal f013n;

    @TableField("F014N")
    private BigDecimal f014n;

    @TableField("F015N")
    private BigDecimal f015n;

    @TableField("F016N")
    private BigDecimal f016n;

    @TableField("Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
    private LocalDateTime modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
