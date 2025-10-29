package com.vv.finance.investment.bg.mapper.index;

import java.util.List;

/**
 * @ClassName: IndexBaseMapper
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/3   9:35
 */
public interface IndexBaseMapper<T> {

    /**
     * 批量插入
     * @param klines
     * @return
     */
    int batchInsert(List<T> klines);
}
