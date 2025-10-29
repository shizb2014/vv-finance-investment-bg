package com.vv.finance.investment.bg.stock.rank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 行业明细
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_industry_subsidiary")
@ApiModel(value="IndustrySubsidiary对象", description="行业明细")
public class IndustrySubsidiary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "行业层级")
    private String level;

    @ApiModelProperty(value = "首字母")
    private String firstRym;

    @ApiModelProperty(value = "行业名称")
    private String name;
    @ApiModelProperty(value = "行业code")
    private String code;

    @ApiModelProperty(value = "平均涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "总成交量")
    private BigDecimal amount;
    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;
    @ApiModelProperty(value = "最新价")
    @TableField(exist = false)
    private BigDecimal last;
    @ApiModelProperty(value = "行情时间")
    private LocalDateTime mktTime;

    @TableField(exist = false)
    private List<StockDefine> stockDefines;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @Override
    public boolean equals(Object o){
        if(o==null){
            return false;
        }
        if(o instanceof IndustrySubsidiary){
            IndustrySubsidiary e=(IndustrySubsidiary)o;
            return e.getCode().equals(this.getCode());
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.getCode().hashCode();
    }
}
