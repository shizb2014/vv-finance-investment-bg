package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
*   @ClassName:    TIndex1minKline
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/27   15:07
*/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_1min_kline")
public class TIndex1minKline extends IndexBaseMinKline {

    private static final long serialVersionUID = 4894995687329888275L;
}