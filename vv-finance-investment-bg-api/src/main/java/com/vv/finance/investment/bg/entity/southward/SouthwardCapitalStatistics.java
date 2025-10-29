package com.vv.finance.investment.bg.entity.southward;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 南向资金每日统计
 * </p>
 *
 * @author qinxi
 * @since 2023-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_southward_capital_statistics")
@ApiModel(value="SouthwardCapitalStatistics对象", description="南向资金每日统计")
public class SouthwardCapitalStatistics implements Serializable {


    private static final long serialVersionUID = 5379876622869219947L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "SH：沪  SZ：深")
    private String market;

    @ApiModelProperty(value = "今日净流入额（带正负）")
    private BigDecimal netTurnoverIn;

    @ApiModelProperty(value = "今日净买入额（带正负）")
    private BigDecimal netBuyingTurnover;

    @ApiModelProperty(value = "今日余额（带正负）")
    private BigDecimal surplusQuota;

    @ApiModelProperty(value = "统计日期")
    private LocalDate statisticsDate;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
