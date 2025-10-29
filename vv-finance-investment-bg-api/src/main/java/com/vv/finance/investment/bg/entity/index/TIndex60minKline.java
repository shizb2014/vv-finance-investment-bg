package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndex60minKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/27   17:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_60min_kline")
public class TIndex60minKline extends IndexBaseMinKline {


}