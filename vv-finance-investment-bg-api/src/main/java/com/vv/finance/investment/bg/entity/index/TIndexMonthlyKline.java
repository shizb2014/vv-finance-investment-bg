package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexMonthlyKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/26   15:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_monthly_kline")
public class TIndexMonthlyKline extends IndexBaseKline {

    private static final long serialVersionUID = -1735926235050245357L;
}