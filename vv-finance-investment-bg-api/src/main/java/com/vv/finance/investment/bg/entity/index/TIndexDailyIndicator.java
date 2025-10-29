package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexDailyIndicator
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   17:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_daily_indicator")
public class TIndexDailyIndicator extends IndexBaseIndicator {

    private static final long serialVersionUID = -4018855691694389139L;
}