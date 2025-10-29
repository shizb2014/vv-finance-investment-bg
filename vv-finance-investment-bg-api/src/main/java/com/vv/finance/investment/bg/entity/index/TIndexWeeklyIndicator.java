package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexWeeklyIndicator
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/27   13:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_weekly_indicator")
public class TIndexWeeklyIndicator extends IndexBaseIndicator {

    private static final long serialVersionUID = 6875680758531114864L;
}