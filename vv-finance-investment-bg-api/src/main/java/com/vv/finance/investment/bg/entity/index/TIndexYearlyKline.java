package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
*   @ClassName:    TIndexYearlyKline
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/10/26   16:02
*/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_yearly_kline")
public class TIndexYearlyKline extends IndexBaseKline {

    private static final long serialVersionUID = 3733881825657656545L;
}