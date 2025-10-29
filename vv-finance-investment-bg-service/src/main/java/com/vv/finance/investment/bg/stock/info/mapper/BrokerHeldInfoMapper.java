package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.BrokerHeldInfo;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 股票码表 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface BrokerHeldInfoMapper extends BaseMapper<BrokerHeldInfo> {

    @Update({
            "update t_broker_held_info set F013N = F013N / #{factor}, Modified_Date = now() where SECCODE = #{stockCode} and F001D = #{f001d}"
    })
    int updateBrokerInfoEvent(@Param("stockCode") String stockCode, @Param("factor") BigDecimal factor, @Param("f001d") Long f001d);

    int updateBrokerInfoEventV2(@Param("stockCode") String stockCode, @Param("factor") BigDecimal factor, List<Long> list);

    @Update({
            "update t_broker_held_info set F013N = F013N_ORG, Modified_Date = now() where SECCODE = #{stockCode}"
    })
    int updateBrokerInfoEventRollBack(@Param("stockCode") String stockCode);

    int batchSave(List<BrokerHeldInfo> brokerStatistics);
}
