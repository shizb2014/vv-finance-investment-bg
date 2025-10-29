package com.vv.finance.investment.bg.stock.info;

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
@TableName("t_broker_held_info")
@ApiModel(value = "t_broker_held_info", description = "")
public class BrokerHeldInfo implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @TableField("SECCODE")
    private String seccode;

    @TableField("F001D")
    private Long f001d;
    @TableField("F002D")
    private Long f002d;

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

    @TableField("F013N_ORG")
    private BigDecimal f013nOrg;

    @TableField("F014N")
    private BigDecimal f014n;

    @TableField("F015N")
    private BigDecimal f015n;

    @TableField("F016N")
    private BigDecimal f016n;

    @TableField("F017N")
    private BigDecimal f017n;

    @TableField("F018N")
    private BigDecimal f018n;

    @TableField("F019N")
    private BigDecimal f019n;

    @TableField("F020N")
    private BigDecimal f020n;

    @TableField("F021N")
    private BigDecimal f021n;

    @TableField("F022N")
    private BigDecimal f022n;

    @TableField("F023N")
    private BigDecimal f023n;

    @TableField("F024N")
    private BigDecimal f024n;

    @TableField("F025N")
    private BigDecimal f025n;
    @TableField("F026N")
    private BigDecimal f026n;

    @TableField("F027N")
    private BigDecimal f027n;

    @TableField("F028N")
    private BigDecimal f028n;

    @TableField("Create_Date")
    private Date createDate;
    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;
}
