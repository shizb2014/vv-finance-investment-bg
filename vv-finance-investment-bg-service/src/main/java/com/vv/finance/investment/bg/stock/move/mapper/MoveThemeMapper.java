package com.vv.finance.investment.bg.stock.move.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.move.StockMove;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author lh.sz
 */
public interface MoveThemeMapper extends BaseMapper<StockMove> {

    void saveBatch(@Param("list") Collection<StockMove> list);
}
