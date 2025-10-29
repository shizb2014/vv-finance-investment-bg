package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @ClassName: TIndexYearlyIndicator
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/27   14:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@TableName(value = "t_index_yearly_indicator")
public class TIndexYearlyIndicator extends IndexBaseIndicator {

    private static final long serialVersionUID = -7722222071743596902L;
}