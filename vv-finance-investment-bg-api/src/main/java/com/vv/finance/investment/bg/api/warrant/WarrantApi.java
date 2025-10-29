package com.vv.finance.investment.bg.api.warrant;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.investment.bg.dto.info.CapitalDistributionVo;
import com.vv.finance.investment.bg.dto.info.DDENetVo;
import com.vv.finance.investment.bg.dto.info.NetRankVo;
import com.vv.finance.investment.bg.dto.info.VolumeStatisticsDTO;
import com.vv.finance.investment.bg.dto.warrant.WarrantCodeVo;

import java.util.List;

/**
 * @ClassName WarrantAPi
 * @Deacription 权证Api
 * @Author lh.sz
 * @Date 2021年12月21日 13:48
 **/
public interface WarrantApi {
    /**
     * 获取经纪席位
     *
     * @param warrantCode 权证代码
     * @param type        类型
     * @return
     */
    ResultT<OrderBrokerDto> getOrderBroker(String warrantCode, String type);

    /**
     * 获取十档数据
     *
     * @param warrantCode 权证代码
     * @return
     */
    ResultT<Order> getOrder(String warrantCode);

    /**
     * 获取权证码表
     *
     * @param sort    排序(up-升序，down-降序)
     * @param sortKey 排序字段
     * @return
     */
    ResultT<List<WarrantCodeVo>> getWarrantCodeList(String sort, String sortKey);

    /**
     * 获取权证快照
     *
     * @param warrantCodes 权证代码
     * @return
     */
    ResultT<List<WarrantSnapshot>> getWarrantSnapshotList(String[] warrantCodes);

    /**
     * 获取权证的主力资金分布
     *
     * @param warrantCode 权证代码
     * @return
     */
    ResultT<List<DDENetVo>> getDdeNetList(String warrantCode);

    /**
     * 获取权证累计资金排名
     *
     * @param warrantCode 权证代码
     * @return
     */
    ResultT<List<NetRankVo>> getNetRank(String warrantCode);

    /**
     * 获取权证资金分布
     *
     * @param warrantCode
     * @return
     */
    ResultT<CapitalDistributionVo> getWarrantCapitalDistribution(String warrantCode);

    /**
     * 获取权证的成交统计
     *
     * @param warrantCode 权证代码
     * @param date        时间
     * @param direction   方向
     * @return
     */
    ResultT<VolumeStatisticsDTO> getWarrantDealStatistics(String warrantCode, Long date, String direction);

}
