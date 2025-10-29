package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.entity.common.StockSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 股票快照 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface StockSnapshotMapper extends BaseMapper<StockSnapshot> {

    int batchSaveOrUpdate(List<StockSnapshot> snapshotList);

    StockSnapshot selectStockDetail(@Param("code") String code);

}
