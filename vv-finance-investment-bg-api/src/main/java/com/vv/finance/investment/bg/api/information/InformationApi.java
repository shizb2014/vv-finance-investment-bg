package com.vv.finance.investment.bg.api.information;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.f10.*;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.information.app.FreeStockNewsVoApp;

import java.util.List;

/**
 * @ClassName InformationApi
 * @Deacription 资讯api
 * @Author lh.sz
 * @Date 2021年09月15日 17:34
 **/
public interface InformationApi {
    /**
     * 分页获取异动资讯
     *
     * @param page 分页组件
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> transactionInformationPage(CommonNewsPage page);

    /**
     * 分页获取异动资讯 v2
     *
     * @param page 页面
     * @return {@link ResultT}<{@link PageWithTime}<{@link FreeStockNewsVo}>>
     */
    ResultT<PageWithTime<FreeStockNewsVo>> transactionInformationPageV2(CommonNewsPage page);

    /**
     * 分页获取新股资讯
     *
     * @param page 分页组件
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> newShareInformationPage(CommonNewsPage page);

    /**
     * 分页获取新股资讯 v2
     *
     * @param page 页面
     * @return {@link ResultT}<{@link PageWithTime}<{@link FreeStockNewsVo}>>
     */
    ResultT<PageWithTime<FreeStockNewsVo>> newShareInformationPageV2(CommonNewsPage page);

    /**
     * 分页获取港美股资讯
     *
     * @param page 分页组件
     * @param type 2港股 3美股
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> hkOrAmericanInformationPage(CommonNewsPage page, CommonNewsPage.QueryCodeEnum type);

    /**
     * 分页获取港美股资讯 v2
     *
     * @param page 页面
     * @param type 类型
     * @return {@link ResultT}<{@link PageWithTime}<{@link FreeStockNewsVo}>>
     */
    ResultT<PageWithTime<FreeStockNewsVo>> hkOrAmericanInformationPageV2(CommonNewsPage page, CommonNewsPage.QueryCodeEnum type);

    /**
     * 查询自选股资讯详情
     *
     * @param newsid
     * @return
     */
    ResultT<StockNewsDetailVo> findFreeDetailById(Long newsid, boolean needWarrant);

    /**
     * 分页查询个股数据
     *
     * @param pageReq
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> listNewsBySimpleStockVo(CommonNewsPage pageReq);

    /**
     * 分页查询个股数据 v2
     *
     * @param pageReq 页面请求
     * @return {@link ResultT}<{@link PageWithTime}<{@link FreeStockNewsVo}>>
     */
    ResultT<PageWithTime<FreeStockNewsVo>> listNewsBySimpleStockVoV2(CommonNewsPage pageReq);

    /**
     * 权证资讯列表
     * @param pageReq
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> listNewsByWarrantStockVo(CommonNewsPage pageReq);

    /**
     * 分页查询7*24资讯
     *
     * @param pageReq
     * @param newType
     * @return
     */
    ResultT<PageWithTime<GroupTwentyFourHourNewsVo>> pageTwentyFourHourNews(NewsPageReq pageReq, Integer newType);

    /**
     * 分页查询7*24资讯 v2
     *
     * @param pageReq 页面请求
     * @param newType 新型
     * @return {@link ResultT}<{@link PageWithTime}<{@link GroupTwentyFourHourNewsVo}>>
     */
    ResultT<PageWithTime<GroupTwentyFourHourNewsVo>> pageTwentyFourHourNewsV2(NewsPageReq pageReq, Integer newType);

    /**
     * 分页查询日历资讯-新股
     *
     * @param pageReq
     * @return
     */
    ResultT<PageNewStockRes<NewShareVo>> pageNewShare(ComCalendarNewsPageReq pageReq);

    /**
     * 分页查询日历资讯-新股
     *
     * @param pageReq
     * @return
     */
    ResultT<ComPageNewStockRes<ComNewShareVo>> pageNewShare4Pc(ComCalendarNewsPageReq pageReq);

    /**
     * 分页查询日历资讯
     *
     * @param pageReq
     * @return
     */
    ResultT<ComPageWithTime<ComInformationGroupVo<ComNewShareVo>>> pageNewShare4App(ComCalendarNewsPageReq pageReq);

    /**
     * 查询次日未上市新股
     *
     * @return {@link List}<{@link ComNewShareVo}>
     */
    List<ComNewShareVo> listNewShare4Check();

    /**
     * 分页查询自选股资讯
     *
     * @param pageReq
     * @param data
     * @return
     */
    ResultT<PageWithTime<FreeStockNewsVo>> listFreeNews(CommonNewsPage pageReq, List<String> data);

    /**
     * 分页查询自选股资讯 v2
     *
     * @param pageReq    页面请求
     * @param stockCodes 股票代码
     * @return {@link ResultT}<{@link PageWithTime}<{@link FreeStockNewsVo}>>
     */
    ResultT<PageWithTime<FreeStockNewsVo>> listFreeNewsV2(CommonNewsPage pageReq, List<String> stockCodes);

    /**
     * 分页查询财经事件
     *
     * @param pageReq
     * @return
     */
    ResultT<PageWithTime<GroupFinancialEventVo>> listFinancialEvents(ComCalendarNewsPageReq pageReq);

    /**
     * 分页查询财经事件 V2
     *
     * @param pageReq 页面请求
     * @return {@link ResultT}<{@link PageWithTime}<{@link GroupFinancialEventVo}>>
     */
    ResultT<PageWithTime<GroupFinancialEventVo>> listFinancialEventsV2(CalendarNewsPageReq pageReq);

    /**
     * 分页查询除权数据
     *
     * @param pageReq
     * @param stocks
     * @return
     */
    ResultT<PageWithTime<GroupExitRightNewsVo>> pageExitRightNews(CalendarNewsPageReq pageReq, List<String> stocks);

    /**
     * 分页查询日历资讯-财报
     *
     * @param code
     * @param pageReq
     * @return
     */
    ResultT<PageWithTime<InformationGroupVo<FinancialReportVo>>> queryNotice(List<String> code, CalendarNewsPageReq pageReq);

    /**
     * 获取新股投资者信息
     * @param stockCode
     * @return
     */
    ResultT<NewStockInvestors> getInvestorInfo(String stockCode);


    ResultT<List<TagDto>> getAllNewsTag();

    ResultT<PageWithTime<FreeStockNewsVo>> assignInformationPageV2(CommonNewsPage page, List<String> stocks);
}
