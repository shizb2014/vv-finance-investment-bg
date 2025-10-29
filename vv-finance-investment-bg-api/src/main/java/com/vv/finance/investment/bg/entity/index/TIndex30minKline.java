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
@TableName(value = "t_index_30min_kline")
public class TIndex30minKline extends IndexBaseMinKline {

    private static final long serialVersionUID = -6214744894141710487L;
}