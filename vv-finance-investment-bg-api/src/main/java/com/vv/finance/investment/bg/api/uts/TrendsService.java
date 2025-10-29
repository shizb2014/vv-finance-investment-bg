package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.entity.quotation.f10.*;
import com.vv.finance.investment.bg.dto.f10.IncrementCompanyEventDTO;
import com.vv.finance.investment.bg.entity.f10.trends.*;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.uts.Xnhks0317;

import java.util.List;
import java.util.Set;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 10:44
 * @Version 1.0
 * 公司动向
 */
public interface TrendsService {

    /**
     * 停复牌
     *
     * @param stockCode
     * @return
     */
    PageDomain<StopAndResume> getStopAndResume(List<String> stockCode, SimplePageReq simplePageReq, Long time);

    /**
     * 停复牌
     *
     * @param stockCode
     * @return
     */
    PageDomain<StopAndResume> getStopAndResume2(String stockCode, SimplePageReq simplePageReq);

    /**
     * 收购及合并
     *
     * @param stockCode
     * @return
     */
    PageDomain<AcquisitionsAndMergers> getAcquisitionsAndMergers(List<String> stockCode, SimplePageReq simplePageReq, Long time);

    /**
     * 收购及合并
     *
     * @param stockCode
     * @return
     */
    PageDomain<AcquisitionsAndMergers> getAcquisitionsAndMergers2(String stockCode, SimplePageReq simplePageReq);

    /**
     * 公司重组
     *
     * @param stockCode
     * @return
     */
    PageDomain<CompanyRecombination> getCompanyRecombination(List<String> stockCode, SimplePageReq simplePageReq, Long time);

    /**
     * 股东大会
     *
     * @param stockCode
     * @return
     */
    PageDomain<GeneralMeeting> getGeneralMeeting(List<String> stockCode, SimplePageReq simplePageReq, Long time);

    /**
     * 股东大会
     *
     * @param stockCode
     * @return
     */
    PageDomain<GeneralMeeting> getGeneralMeeting2(String stockCode, SimplePageReq simplePageReq);

    /**
     * 交易警报
     *
     * @param stockCode
     * @return
     */
    PageDomain<TransactionAlert> getTransactionAlert(
            List<String> stockCode,
            SimplePageReq simplePageReq,
            Long time
    );

    /**
     * 交易警报
     *
     * @param stockCode
     * @return
     */
    PageDomain<TransactionAlert> getTransactionAlert2(String stockCode, SimplePageReq simplePageReq);

    /**
     * 并行交易
     *
     * @param stockCode
     * @return
     */
    PageDomain<TransactionParallelism> getTransactionParallelism(
            List<String> stockCode,
            SimplePageReq simplePageReq,
            Long time
    );

    /**
     * 并行交易
     *
     * @param stockCode
     * @return
     */
    PageDomain<TransactionParallelism> getTransactionParallelism2(String stockCode, SimplePageReq simplePageReq);

    /**
     * 财报
     *
     * @param codes
     * @return
     */
    PageDomain<String> listByPageF10Event(
            List<String> codes,
            SimplePageReq pageReq,
            Long time
    );

    /**
     * 公司动向
     *
     * @param time
     * @return
     */
    Set<String> listCompanyEventStockCodes(Long time);

    List<IncrementCompanyEventDTO> listIncrementCompanyEventStockCodeAndTime(
            Long beginTime,
            Long endTime
    );

    /**
     * 查询最新的五条数据
     *
     * @param stockCode
     * @return
     */
    List<CompanyTrendAppVo> getRecentByCode(String stockCode);

    /**
     * 单个股票公司动向分页
     *
     * @param pageReq
     * @param stockCode
     * @return
     */
    ResultT<PageDomain<CompanyTrendAppVo>> pageCompanyTrends(SimplePageReq pageReq, String stockCode);

    /**
     * 公司动向分页
     *
     * @param pageReq   页面要求
     * @param stockCode 股份代号
     * @return {@link ResultT}<{@link PageDomain}<{@link ComCompanyTrendVo}>>
     */
    ResultT<ComPageWithTime<ComInformationGroupVo<ComCompanyTrendAppVo>>> pageF10CompanyTrendsApp(ComCalendarNewsPageReq pageReq, String stockCode);

    /**
     * 日历资讯-公司动向
     * 合并多个动向类型的数据
     *
     * @return
     * @Deprecated 已经改成使用 canal 同步的方案了，这个方法不应该再调用。
     * 以前这个方法由 xxljob 触发
     * 全量同步改为
     * @see #fullSyncCompanyTrends()
     */
    @Deprecated
    ResultT mergeCalendarInformation();

    ResultT mergeCalendarInformationOld();

    /**
     * 分页查询公司动向
     *
     * @param pageReq
     * @param stocks
     * @return
     */
    ResultT<PageWithTime<InformationGroupVo<CompanyTrendPcVo>>> pageCompanyTrends(CalendarNewsPageReq pageReq, List<String> stocks);


    /**
     * 分页查询公司动向
     *
     * @param pageReq 分页请求
     * @param isApp   是否app调用，app不查询财报和除权
     * @param stocks  股票
     * @return {@link ResultT}<{@link ComPageWithTime}<{@link ComInformationGroupVo}<{@link ComCompanyTrendVo}>>>
     */
    ResultT<ComPageWithTime<ComInformationGroupVo<ComCompanyTrendVo>>> pageCompanyTrendsNew(ComCalendarNewsPageReq pageReq, boolean isApp, List<String> stocks);

    ResultT<PageWithTime<InformationGroupVo<CompanyTrendAppVo2>>> pageCompanyTrends4App(CalendarNewsPageReq pageReq, List<String> stocks);

    void fullSyncCompanyTrends();

    ResultT<List<ComNewShareCalendarVO>> CompanyTrendsCalendar(ComNews comNews);
    /**
     * 删除临时股票公司动向
     *
     * @param stockCode
     */
    void delCompanyTrendByStockCode(String stockCode);
    /**
     * 变更公司动向股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    void upCompanyTrendStockCode(String sourceCode, String targetCode);


    /**
     * 新增F10数据
     *
     * @param stockCode 代码
     */
    void createCompanyTrendByStockCode(String stockCode);
}
