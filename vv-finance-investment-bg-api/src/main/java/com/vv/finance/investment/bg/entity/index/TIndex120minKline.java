package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndex30minKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/27   17:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_120min_kline")
public class TIndex120minKline extends IndexBaseMinKline {


}