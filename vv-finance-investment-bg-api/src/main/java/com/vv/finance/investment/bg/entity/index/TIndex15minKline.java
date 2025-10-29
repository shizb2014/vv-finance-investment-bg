package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndex15minKline
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/27   17:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_15min_kline")
public class TIndex15minKline extends IndexBaseMinKline {

    private static final long serialVersionUID = -628856474058541951L;
}