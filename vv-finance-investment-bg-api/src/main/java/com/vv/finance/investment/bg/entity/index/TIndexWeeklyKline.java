package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexWeeklyKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/26   15:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_weekly_kline")
public class TIndexWeeklyKline extends IndexBaseKline {

    private static final long serialVersionUID = -4952415871812027298L;
}