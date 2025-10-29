package com.vv.finance.investment.bg.api.stock;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.dto.ComStockRelationDto;
import com.vv.finance.common.dto.QueryComSimpleStockDefineDto;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.PreAdjPriceInfo;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import com.vv.finance.common.entity.quotation.StockDefinePageReq;
import com.vv.finance.investment.bg.dto.f10.F10PageBaseReq;
import com.vv.finance.investment.bg.dto.info.StockSimpleInfo;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.dto.SuspensionDto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hamilton
 * @date 2020/10/27 16:40
 */
public interface StockInfoApi {
    /**
     * 保存股票码表
     *
     * @param stockDefineList
     * @return
     */
    ResultT<Void> saveStockInfo(boolean saveAll, List<StockDefine> stockDefineList);

    /**
     * 保存股票码表
     *
     * @param stockDefine
     * @return
     */
    ResultT<Void> saveStockInfo(StockDefine stockDefine);

    /**
     * 发送更新消息
     *
     * @return {@link ResultT }<{@link Void }>
     */
    ResultT<Void> sendUpdateMessage();

    /**
     * 更改股票停牌标识
     *
     * @param suspensionDto
     * @return
     */
    ResultT<Void> updateSuspension(SuspensionDto suspensionDto);


    /**
     * 查询股票信息
     *
     * @param code
     * @return
     */
    ResultT<StockDefine> queryStockDefine(String code);

    /**
     * 根据行业代码获取股票列表
     *
     * @param industryCode
     * @return
     */
    ResultT<List<StockDefine>> queryStockListByIndustry(String industryCode);

    /**
     * 股票信息  size pageNum 为0查询全部
     *
     * @param size    0
     * @param pageNum 0
     * @param market  市场代码 MAIN
     * @return
     */
    ResultT<Page<StockDefine>> listStockDefine(Integer size, Integer pageNum, String market);


    /**
     * 分页查询港股码表
     * @param req 请求参数
     * @return
     */
    ResultT<PageDomain<StockDefine>> pageStockDefine(StockDefinePageReq req);

    ResultT<PageDomain<StockDefine>> pageStockDefine(F10PageBaseReq f10PageBaseReq );



    /**
     * 股票代码与行业代码对应 key--stockCode val--IndustryCode
     *
     * @return
     */
    Map<String, String> stockRelationIndustryCode();

    ResultT<List<String>> allStockDefineCodes();

    ResultT<List<StockDefine>> getStockDefineByStockType(Integer stockType);

    List<String> stockCodes();

    List<Object> allStockCodes();

    List<String> tradeStockCodes();

    ResultT<List<String>> getStockCodeBySuspension(Integer suspension);


    void initStockDefine();

    List<StockDefine> getAllStockDefines(List<String> codeAll);

    /**
     * 获取所有股票的上市时间
     *
     * @return
     */
    ResultT<Map<String, String>> getAllStockListingDate();

    /**
     * 获取股票资讯信息
     *
     * @return
     */
    ResultT<StockNewsEntity> getStockNewsById(Long stockNewsId);

    /**
     * 获取股票证券名称
     *
     * @return {@link Map}<{@link String}, {@link String}>
     */
    Map<String, String> getStockCodeNameMap();


    /**
     * 本地缓存中获取股票基本信息 批量
     *
     */
    Map<String, ComSimpleStockDefine> getStockSimpleInfos();

    /**
     * 本地缓存中获取股票基本信息 批量
     *
     */
    Map<Long, ComSimpleStockDefine> getStockIdSimpleInfos();

    /**
     * 本地缓存中获取股票基本信息 个股
     * @return
     */
    ComSimpleStockDefine getStockSimpleInfoByStockId(Long stockId);

    /**
     * 本地缓存中获取股票基本信息 批量
     *
     */
    List<ComStockSimpleDto> getStockSimpleInfoLists();

    /**
     * 本地缓存中获取股票基本信息 批量
     *
     * @param stockIds 股票 IDS
     * @return {@link List }<{@link ComSimpleStockDefine }>
     */
    List<ComSimpleStockDefine> getStockInfoListByStockIds(List<Long> stockIds);

    /**
     * 根据查询条件从本地缓存中获取股票基本信息
     *
     */
    Map<String, ComSimpleStockDefine> getStockSimpleInfos(QueryComSimpleStockDefineDto query);

    /**
     * 根据stockIds获取stockCodes
     * @param stockIds
     * @return
     */
    Set<String> getStockCodesByStockIds(Set<Long> stockIds);
    /**
     * 本地缓存中获取股票基本信息 个股
     * @return
     */
    ComSimpleStockDefine getStockSimpleInfo(String code);

    /**
     * 获取当前交易时段
     * @return
     */
    Integer getTradingPeriod();

    /**
     * 获取板块快照数据
     * @return
     */
    ResultT<Boolean> getBlockSnapshot();
    /**
     * 判断code是否为行业或概念
     */
    Boolean industryFlag(String code);

    /**
     * 需要落库行业code
     * @return
     */
    ResultT<List<String>> listKlineIndustry();

    /**
     * 计算前复权价格
     * @return
     */
    List<PreAdjPriceInfo> calcForwardPrice ( List<PreAdjPriceInfo> preAdjPriceInfos);

    /**
     * 根据code批量查询stockId(查询所有)
     *
     * @param codes 代码
     * @return {@link Map }<{@link String }, {@link Long }>l
     */
    Map<String, Long> selectStockIdAllByCodes(List<String> codes);

    /**
     * 根据code批量查询stockId(查询数据库，近状态为0)
     *
     * @param codes 代码
     * @return {@link Map }<{@link String }, {@link Long }>l
     */
    Map<String, Long> selectStockIdMapByCodes(List<String> codes);

    /**
     * 根据code批量查询stockId(查询缓存)
     *
     * @param codes 代码
     * @return {@link Map}<{@link String}, {@link Long}>
     */
    Map<String, Long> selectStockIdByCodes(List<String> codes);

    /**
     * 更新股票关联关系，仅 bizType 为 1、2、3 时生效； bizType为其他值返回null<br/>
     * 1. 临时股票: bizType=1
     * 2. 代码复用: bizType=2
     * 3. 转板: bizType=3
     *
     * @param relationDtoList 关系 DTO
     * @return {@link Map}<{@link String}, {@link Long}> <innerCode, stockId>
     */
    Map<String, Long> updateStockRelations(List<ComStockRelationDto> relationDtoList);

    /**
     * 获取stockId，新股自动生成stockId并同步基本信息
     *
     * @param relationDto 关系 DTO
     * @return {@link Long}
     */
    Long buildStockId(ComStockRelationDto relationDto);

    /**
     * 根据code批量查询创建时间
     *
     * @param codes 代码
     * @return {@link Map}<{@link String}, {@link Date}>
     */
    Map<String, Date> selectTimeByCodes(List<String> codes);

    /**
     * 批量查询复用之后的code
     *
     * @param codes 代码
     * @return {@link Map}<{@link String}, {@link String}>
     */
    Map<String, String> selectReuseCodeMap(List<String> codes);

    /**
     * 根据股票code和业务时间获取该业务时间时的股票id
     *
     * @param  codeTimeMap  List<Pair<String, String>> left-stockCode  right-timeStr(yyyy-MM-dd)
     *
     * @return Map<Pair<String, String>, Long>，key-Pair<stockCode, timeStr>，value-stockId
     */
    Map<Pair<String, String>, Long> getStockIdByCodeAndTime(List<Pair<String, String>> codeTimeMap);
    /**
     * 获取指定日期发生转板/代码复用的股票信息
     *
     * @param bizTypes 业务类型，2-代码复用，3-转板
     * @param bizTime 业务时间，格式：yyyyMMdd，如临时股票存并行交易结束时间，代码复用或转板存对应变更时间
     * @return
     */
    List<HkStockRelation> getReusOrConversionStock(List<Integer> bizTypes, String bizTime);
    /**
     * 变更盘口数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void updateOrderStockCode(String sourceCode, String targetCode);

    /**
     * 变更经济席位数据
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void updateEconomy(String sourceCode, String targetCode);

    void updateSimulateEconomy(String sourceCode, String targetCode);
    /**
     * 删除临时股票经济席位数据
     *
     * @param code 时股票
     */
    void delEconomy(String code);

    /**
     * 删除临时股票盘口数据
     *
     * @param code 时股票
     */
    void delOrderStockCode(String code);
    /**
     * 新增模拟股票盘口数据
     *
     * @param simulateCode 模拟股票code
     */
    void saveSimulateOrderInfo(String simulateCode);
    /**
     * 校验股票id并返回无效的股票id
     *
     * @param stockIds 股票id集合
     */
    Set<Long> checkStockIdAndGetInvalidStockId(Set<Long> stockIds);
}
