package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
*   @ClassName:    TIndex5minKline
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/27   15:48
*/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_5min_kline")
public class TIndex5minKline extends IndexBaseMinKline {

    private static final long serialVersionUID = -2427124300099846300L;
}