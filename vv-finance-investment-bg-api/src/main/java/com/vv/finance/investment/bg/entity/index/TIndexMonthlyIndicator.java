package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
*   @ClassName:    TIndexMonthlyIndicator
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/27   13:41
*/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_monthly_indicator")
public class TIndexMonthlyIndicator extends IndexBaseIndicator {

    private static final long serialVersionUID = -3229731164153431257L;
}