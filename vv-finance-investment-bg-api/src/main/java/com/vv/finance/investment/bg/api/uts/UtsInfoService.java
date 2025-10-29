package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.f10.ComCompanyTrendVo;
import com.vv.finance.common.entity.quotation.f10.ComFinancialNotifyVO;
import com.vv.finance.common.us.entity.bg.StockNoticeTitle;
import com.vv.finance.investment.bg.dto.uts.resp.*;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk1002;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import com.vv.finance.investment.bg.entity.uts.Xnhks0503;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author chenyu
 * @date 2021/3/2 15:25
 */
public interface UtsInfoService {

    /**
     * 获取历史
     * @param date
     * @return
     */
    List<Xnhk0127> getXnhk0127History(Date date);

    /**
     * 获取当前时间的数据
     *
     * @param date
     * @return
     */
    List<Xnhk0127> getXnhk0127(Date date);

    List<Xnhk0127> listXnhk0127(
        Date date,
        List<String> codes
    );

    List<Xnhk0127> listXnhk0127ByDay(
            Date date,
            List<String> codes
    );

    List<Xnhk0127> listXnhk0127ByF002V(List<String> codes, List<String> f002Vs);

    Xnhk0127 getXnhk0127ByF002V(String code, List<String> f002Vs);

    List<Xnhk0127> getXnhk0127NotIncludeSS(Date startDate, Date endDate);

    List<Xnhk0127> getXnhk0127IncludeSS(Date startDate, Date endDate);

    List<Xnhk0127> getXnhk0127DuplicateData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<Xnhk0127> getXnhk0127sByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 根据股票获取招股价
     *
     * @return
     */
    List<Xnhks0503> getXnhks0503ByCodes(
        Set<String> codes,
        Long date
    );

    /**
     * 根据股票获取招股信息
     * @param code
     * @return
     */
    Xnhks0503 getXnhks0503ByCode(String code);

    /**
     * 获取今日的新股
     * @param date
     * @return
     */
    List<Xnhks0503> getXnhks0503ByTime(Long date);

    List<Xnhk0127> getXnhk0127ByCode(String code);

    /**
     * 获取财报列表
     * @param date
     * @param codes
     * @return
     */
    List<String> listFinCodes(
        Date date,
        List<String> codes
    );

    /**
     * 获取财报列表(新获取方式:20220609(因为发现那些的公布日期都在融聚汇给我们把这个数据传过来之前，就导致一直不能触发了，比如我6月3号才得知6月2号发布了财报。))
     * @param date
     * @param codes
     * @return
     */
    List<String> listFinCodesV2(
            Date date,
            List<String> codes
    );

    /**
     * 获取公司概况
     * @param code
     * @return
     */
    StockUtsBasicFactsResp getBasicFacts(String code);

    /**
     * 获取公司简况
     * @param code
     * @return
     */
    StockUtsBriefingResp getBriefing(String code);

    /**
     * 获取公告列表
     * @param type
     * @param code
     * @param currentPage
     * @param pageSize
     * @return
     */
    PageDomain<StockUtsNoticeListResp> listNotice(Integer type, String code, Integer currentPage, Integer pageSize);

    /**
     * 获取公告列表（已有附件）
     * @param type
     * @param code
     * @param currentPage
     * @param pageSize
     * @return
     */
    PageDomain<StockUtsNoticeListResp> listNoticeByMongo(Integer type, String code, Integer currentPage, Integer pageSize);

    /**
     * 获取指数及指数成分股
     * @return
     */
    ResultT<List<Xnhk1002>> listMembersAndIndex();

    ResultT<List<ReuseTempDTO>> listResultTempList();

    /**
     * 获取指定日期的所有派息的股票
     * @return
     */
    ResultT<List<Xnhk0127>> queryDividendStock(Long queryDate);
    /**
     * 获取指定结束交易日期的临时股票集合
     * @return
     */
    ResultT<List<ReuseTempDTO>> findEndTradeTempStock(Date time);
    /**
     * 获取指定交易日期内有交易的临时股票集合
     * @return
     */
    List<ReuseTempDTO> findTradingTempStockByTime(Date time);

    /**
     * 获取所有临时股票集合（去重）
     * @return
     */
    List<ReuseTempDTO> findAllTempStocks();

    /**
     * 获取所有未交易临时股票集合（去重）
     * @return
     */
    List<String> findAllUnTradeTempSocks(Date time);

    /**
     * 根据code获得历史除权数据
     * @param date
     * @return
     */
    List<Xnhk0127> getXnhk0127HistoryByCode(Date date,String code);
    /**
     * 获取公告类型列表
     * @return
     */
    List<StockNoticeTitle> getNoticeTitle();
    /**
     * 删除临时股票公告数据
     * @param stockCode
     * @return
     */
    void delNoticeByStockCode(String stockCode);
    /**
     * 变更公告股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    void upNoticeStockCode(String sourceCode, String targetCode);
    /**
     * 获取code集合获取临时交易信息集合
     * @return
     */
    List<ReuseTempDTO> findTempStockInfoByCodes(List<String> codes);
    /**
     * 新增模拟股票公告数据
     *
     * @param simulateCode 模拟股票code
     */
    void saveSimulateNoticeInfo(String simulateCode);

    /**
     * 处理uts数据（增、删、改）
     *
     * @param oldStockCode 老股票代码
     * @param newStockCode 新股票代码, 如果是新增或删除，此字段为空
     * @param commandType  命令类型 {@link SqlCommandType}
     */
    void handleUtsDataByCodeAndType(String oldStockCode, String newStockCode, String commandType);


    /**
     * 查询财报（个股提醒使用）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param codes     代码
     * @return {@link List }<{@link ComFinancialNotifyVO }>
     */
    List<ComFinancialNotifyVO> listFinancialReportList(Date startDate, Date endDate, List<String> codes);

    /**
     * 股东大会-用于个股提醒
     *
     * @param date  日期
     * @param codes 代码
     * @return {@link List }<{@link ComCompanyTrendVo }>
     */
    List<ComCompanyTrendVo> listNotifyGeneralMeeting(Date date, List<String> codes);

    /**
     * 停牌-用于个股提醒
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param codes     代码
     * @return {@link List }<{@link ComCompanyTrendVo }>
     */
    List<ComCompanyTrendVo> listNotifySuspension(Date startDate, Date endDate, List<String> codes);

    /**
     * 复牌-用于个股提醒
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param codes     代码
     * @return {@link List }<{@link ComCompanyTrendVo }>
     */
    List<ComCompanyTrendVo> listNotifyResumption(Date startDate, Date endDate, List<String> codes);

    /**
     * 并行交易-用于个股提醒
     *
     * @param date  日期
     * @param codes 代码
     * @return {@link List }<{@link ComCompanyTrendVo }>
     */
    List<ComCompanyTrendVo> listNotifyParallel(Date date, List<String> codes);

    /**
     * 获取全部股权股票 包含模拟股票
     * @return
     */
    List<StockRightsDTO> getAllStockRightsAndMock();

    /**
     * 获取全部股权股票
     * @return
     */
   List<StockRightsDTO> getAllStockRights();

    /**
     * 获取指定日期正在交易的股权股票
     * @return
     */
    List<StockRightsDTO> getTradingStockRights(Date time);

    /**
     * 获取指定日期开始交易的股权股票
     * @return
     */
    List<StockRightsDTO> getStartTradingStockRights( Date time);
    /**
     * 获取指定日期结束交易的股权股票
     * @return
     */
    List<StockRightsDTO> getEndTradingStockRights( Date time);
    /**
     * 获取指定日期不再进行交易的股权股票
     * @return
     */
    List<StockRightsDTO> getUnTradingStockRights(Date time);

}
