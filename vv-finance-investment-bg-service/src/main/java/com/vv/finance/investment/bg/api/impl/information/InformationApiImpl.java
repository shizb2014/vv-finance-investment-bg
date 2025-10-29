package com.vv.finance.investment.bg.api.impl.information;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.f10.*;
import com.vv.finance.investment.bg.api.information.InformationApi;
import com.vv.finance.investment.bg.api.warrant.WarrantApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandler;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandlerV2;
import com.vv.finance.investment.bg.stock.information.service.StockInformationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName InformationApiImpl
 * @Deacription 资讯实现
 * @Author lh.sz
 * @Date 2021年09月15日 17:45
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class InformationApiImpl implements InformationApi {

    @Resource
    private StockInformationServiceImpl service;

    @Resource
    InformationHandler informationHandler;

    @Resource
    InformationHandlerV2 informationHandlerV2;

    @Resource
    private StockNewsMapper stockNewsMapper;

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> transactionInformationPage(CommonNewsPage page) {
        return ResultT.success(informationHandler.transactionInformationPage(page));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> transactionInformationPageV2(CommonNewsPage page) {
        return ResultT.success(informationHandlerV2.transactionInformationPageV2(page));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> newShareInformationPage(CommonNewsPage page) {
        return ResultT.success(informationHandler.newShareInformationPage(page));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> newShareInformationPageV2(CommonNewsPage page) {
        return ResultT.success(informationHandlerV2.newShareInformationPageV2(page));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> hkOrAmericanInformationPage(CommonNewsPage page,
                                                                              CommonNewsPage.QueryCodeEnum type) {
        return ResultT.success(informationHandler.hkOrAmericanInformationPage(page, type));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> hkOrAmericanInformationPageV2(CommonNewsPage page, CommonNewsPage.QueryCodeEnum type) {
        return ResultT.success(informationHandlerV2.hkOrAmericanInformationPageV2(page, type));
    }

    @Override
    public ResultT<StockNewsDetailVo> findFreeDetailById(Long newsid, boolean needWarrant) {
        StockNewsDetailVo byNewsid = informationHandler.findByNewsidInHk(newsid,needWarrant);
        return ResultT.success(byNewsid);
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> listNewsBySimpleStockVo(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> stockNewsVoPage = informationHandler.listNewsBySimpleStockVo(pageReq);
        return ResultT.success(stockNewsVoPage);
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> listNewsBySimpleStockVoV2(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> stockNewsVoPage = informationHandlerV2.listNewsBySimpleStockVoV2(pageReq);
        return ResultT.success(stockNewsVoPage);
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> listNewsByWarrantStockVo(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> stockNewsVoPage = informationHandler.listNewsByWarrantStockVo(pageReq);
        return ResultT.success(stockNewsVoPage);
    }

    @Override
    public ResultT<PageWithTime<GroupTwentyFourHourNewsVo>> pageTwentyFourHourNews(NewsPageReq pageReq, Integer newType) {
        PageWithTime<GroupTwentyFourHourNewsVo> twentyFourHourNewsPage = service.pageTwentyFourHourNews(pageReq, newType);
        return ResultT.success(twentyFourHourNewsPage);

    }

    @Override
    public ResultT<PageWithTime<GroupTwentyFourHourNewsVo>> pageTwentyFourHourNewsV2(NewsPageReq pageReq, Integer newType) {
        PageWithTime<GroupTwentyFourHourNewsVo> twentyFourHourNewsPage = informationHandlerV2.pageTwentyFourHourNewsV2(pageReq, newType);
        return ResultT.success(twentyFourHourNewsPage);

    }

    @Override
    @Deprecated
    public ResultT<PageNewStockRes<NewShareVo>> pageNewShare(ComCalendarNewsPageReq pageReq) {
        PageNewStockRes<NewShareVo> res = new PageNewStockRes<>();
        ComPageNewStockRes<ComNewShareVo> comNewShareVoComPageNewStockRes = service.pageNewShare(pageReq);
        BeanUtils.copyProperties(comNewShareVoComPageNewStockRes,res);
        return ResultT.success(res);
    }

    @Override
    public ResultT<ComPageNewStockRes<ComNewShareVo>> pageNewShare4Pc(ComCalendarNewsPageReq pageReq) {
        return ResultT.success(service.pageNewShare(pageReq));
    }

    @Override
    public ResultT<ComPageWithTime<ComInformationGroupVo<ComNewShareVo>>> pageNewShare4App(ComCalendarNewsPageReq pageReq) {
        return ResultT.success(service.pageNewShare4App(pageReq));
    }

    @Override
    public List<ComNewShareVo> listNewShare4Check() {
        return service.listNewShare4Check();
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> listFreeNews(CommonNewsPage pageReq, List<String> stockCodes) {
        return ResultT.success(informationHandler.pageFreeVo(pageReq, stockCodes));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> listFreeNewsV2(CommonNewsPage pageReq, List<String> stockCodes) {
        return ResultT.success(informationHandlerV2.pageFreeVoV2(pageReq, stockCodes));
    }

    @Override
    public ResultT<PageWithTime<GroupFinancialEventVo>> listFinancialEvents(ComCalendarNewsPageReq pageReq) {

        return ResultT.success(service.pageFinancialEvent(pageReq));
    }

    @Override
    public ResultT<PageWithTime<GroupFinancialEventVo>> listFinancialEventsV2(CalendarNewsPageReq pageReq) {
        return ResultT.success(informationHandlerV2.pageFinancialEventV2(pageReq));
    }

    @Override
    public ResultT<PageWithTime<GroupExitRightNewsVo>> pageExitRightNews(CalendarNewsPageReq pageReq, List<String> stocks) {

        return ResultT.success(service.pageExitRightNews(pageReq, stocks));
    }

    @Override
    public ResultT<PageWithTime<InformationGroupVo<FinancialReportVo>>> queryNotice(List<String> code, CalendarNewsPageReq pageReq) {

        PageWithTime<InformationGroupVo<FinancialReportVo>> pageDomain = service.queryNotice(code, pageReq);
        return ResultT.success(pageDomain);
    }

    @Override
    public ResultT<NewStockInvestors> getInvestorInfo(String stockCode) {
        return ResultT.success(service.getInvestorsInformation(stockCode));
    }

    @Override
    public ResultT<List<TagDto>> getAllNewsTag() {
        List<TagDto> tagDtos = stockNewsMapper.listAllTags();
        return ResultT.success(tagDtos);
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVo>> assignInformationPageV2(CommonNewsPage page, List<String> stocks) {
        return ResultT.success(informationHandlerV2.assignInformationPageV2(page, stocks));
    }

}
