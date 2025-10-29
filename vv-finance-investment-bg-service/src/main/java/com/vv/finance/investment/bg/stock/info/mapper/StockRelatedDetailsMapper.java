package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName StockRelatedDetailsMapper
 * @Deacription 股票相关数据
 * @Author lh.sz
 * @Date 2021年12月27日 11:38
 **/
public interface StockRelatedDetailsMapper extends BaseMapper<StockRelatedDetails> {
    /**
     * 更新快照通过code
     *
     * @param entity
     */
    @Update({
            "update t_stock_related_details set snapshot_details = #{snapshotDetails} where code = #{code} "
    })
    int updateSnapshotDetailByCode(StockRelatedDetails entity);
}
