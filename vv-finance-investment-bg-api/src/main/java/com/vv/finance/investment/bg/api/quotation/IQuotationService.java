package com.vv.finance.investment.bg.api.quotation;

import com.vv.finance.common.entity.common.IndexComponentDistribute;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.receiver.Order;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: IQuotationService
 * @Description:
 * @Author: Demon
 * @Datetime: 2021/5/31   16:18
 */
public interface IQuotationService {

    /**
     * 获取股票及名字列表
     *
     * @return
     */
    Set<String> selectStockNameList();

    /**
     * 获取股票列表
     *
     * @return
     */
    Set<String> selectStockCodeList();

    /**
     * 获取压缩股票快照集合
     *
     * @return
     */
    Map<String, String> selectCompressStockMap();

    /**
     * 根据股票获取快照
     *
     * @param code
     * @return
     */
    StockSnapshot selectStockSnapshot(String code);

    /**
     * 查询order
     *
     * @param code
     * @return
     */
    Order selectOrder(String code);

    /**
     * IndexComponentDistribute
     *
     * @param code
     * @return
     */
    IndexComponentDistribute selectIndexComponentDistribute(String code);


    /**
     * push项目行情推送
     *
     * @param pushKey
     * @param message
     */
    void quotationPush(String pushKey, Object message);

    /**
     * 获取所有快照
     * @return
     */
    List<StockSnapshot> getAllStockSnapshot();
    /**
     * 获取所有快照map集合
     * @return
     */
    Map<String, Object> getTodayStockSnapshotMap();


}
