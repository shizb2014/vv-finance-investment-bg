package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author lh.sz
 * @since 2022-01-19
 */
@Data
@Accessors(chain = true)
public class Xnhk0901 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;

    @TableField("F002N")
    private BigDecimal f002n;

    /**
     * 历史最高
     */
    @TableField("F003N")
    private BigDecimal f003n;

    /**
     * 历史最低
     */
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

    @TableField("F009V")
    private String f009v;

    @TableField("F010N")
    private BigDecimal f010n;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
