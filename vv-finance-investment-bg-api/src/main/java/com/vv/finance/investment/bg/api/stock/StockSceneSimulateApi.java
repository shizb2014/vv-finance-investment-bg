package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.common.ComSceneReq;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auther: shizb
 * @Date: 2024/7/15
 * @Description: com.vv.finance.investment.bg.api.stock  港股特殊场景
 * @version: 1.0
 */
public interface StockSceneSimulateApi {

    /**
     * 并行交易结束次日 通过日期获取当天结束的并行交易的股票代码
     * 注意：这里我查询的就是传入进来的日期
     */
    ResultT<List<ReuseTempDTO>> findEndTradeTempSimulateStock(Date date);

    /**
     * 并行交易开始当天 通过日期获取当天开始的并行交易的股票代码
     */
    List<ReuseTempDTO> findTradingTempSimulateStockByTime(Date date);


    /**
     * 港股代码复用场景，获取指定日期发生的代码复用code
     *
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    ResultT<List<String>> getSimulateStockRepeat(Date date);

    /**
     * 港股转板代码变更场景，获取指定日期发生的代码变更记录
     *
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    ResultT<Map<String, String>> getSimulateStockConversionMarket(String date);

    /**
     * 特殊场景造数据
     * @param comSceneReq
     * @return
     */
    ResultT saveDataSceneByCode(ComSceneReq comSceneReq);

    /**
     * 删除模拟数据
     *
     * @param code   股票代码
     */
    void deleteDataSceneByCode(boolean isMock, String code);

    /**
     * 交易使用，拷贝k线，盘口，快照
     *
     * @param code  股票代码
     */
    void copyQuotationByCode(String code);

    /**
     * 将已经触发过特殊场景得记录修改为1970
     * @param codes
     * @return
     */
    ResultT updateSceneDate(List<String> codes);

    /**
     * 获取指定日期开始交易的股权股票
     * @return
     */
    List<StockRightsDTO> getStartTradingStockRights(Date time);
    /**
     * 获取指定日期结束交易的股权股票
     * @return
     */
    List<StockRightsDTO> getEndTradingStockRights( Date time);
}
