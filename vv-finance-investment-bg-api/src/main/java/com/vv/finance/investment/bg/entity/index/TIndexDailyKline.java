package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexDailyKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/26   15:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_daily_kline")
public class TIndexDailyKline extends IndexBaseKline {

    private static final long serialVersionUID = 2351061327172053914L;

}