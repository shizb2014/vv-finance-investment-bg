package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.investment.bg.stock.info.mapper.StockSnapshotMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 股票快照 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
@Service
public class StockSnapshotServiceImpl extends ServiceImpl<StockSnapshotMapper, StockSnapshot>
    implements IStockSnapshotService {
    @Autowired
    private StockSnapshotMapper mapper;

    @Override
    public boolean batchSaveOrUpdate(List<StockSnapshot> snapshotList) {
        return this.baseMapper.batchSaveOrUpdate(snapshotList) > 0;
    }
}
