package com.vv.finance.investment.bg.api.impl.f10;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.f10.F10MonitorPlateApi;
import com.vv.finance.investment.bg.dto.f10.NewStockEventTimeLineDTO;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import com.vv.finance.investment.bg.entity.uts.Xnhks0503;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：工作台-监听-板块
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/8/31 15:44
 * @版本：1.0
 */

@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice", validation = "true")
@RequiredArgsConstructor
@Slf4j
public class F10MonitorPlateApiImpl extends AbstractBaseServiceImpl implements F10MonitorPlateApi {
    private final Xnhks0112Mapper xnhks0112Mapper;
    private final Xnhks0502Mapper xnhks0502Mapper;
    private final Xnhks0501Mapper xnhks0501Mapper;
    private final Xnhks0503Mapper xnhks0503Mapper;
    private final NewStockTimeLineMapper newStockTimeLineMapper;

    @Override
    public List<String> listAllStockCodes(
    ) {
        return getSnapshot().stream().map(StockSnapshot::getCode).collect(Collectors.toList());
    }

    @Override
    public List<Xnhks0112> listXrEvent(
        List<String> codes,
        Long time
    ) {
        Long date = DateUtils.formatDateToLong(new Date(time), null);
        return xnhks0112Mapper.selectList(
            new QueryWrapper<Xnhks0112>().in(codes != null && !codes.isEmpty(), "SECCODE", codes)
                .and(a -> a.eq("F001D", date).or().eq("F016D", date).or().eq("F010D", date).or().eq("F015D", date)));
    }

    @Override
    public List<Xnhks0112> listIncrementXrEvent(
        Long beginTime,
        Long endTime
    ) {
        Long beginDate = DateUtils.formatDateToLong(new Date(beginTime), null);
        Long endDate = DateUtils.formatDateToLong(new Date(endTime), null);
        return xnhks0112Mapper.selectList(new QueryWrapper<Xnhks0112>().and(
            a -> a.ge("Create_Date", beginDate).le("Create_Date", endDate).or().ge("Modified_Date", beginDate)
                .le("Modified_Date", endDate)));
    }

    @Override
    public List<NewStockEventTimeLineDTO> listNewStockTimeLine(
        List<String> codes,
        Long time
    ) {
        Long date = DateUtils.formatDateToLong(new Date(time), null);
        List<NewStockEventTimeLineDTO> newStockTimeLines = newStockTimeLineMapper.listNewStockTimeLine(date);
        return buildNewStockTimeLines(newStockTimeLines);
    }

    @Override
    public List<NewStockEventTimeLineDTO> listIncrementNewStockTimeLine(
        Long beginTime,
        Long endTime
    ) {
        List<NewStockEventTimeLineDTO> newStockTimeLines =
            newStockTimeLineMapper.listIncrementNewStockTimeLine(new Date(beginTime), new Date(endTime));
        return buildNewStockTimeLines(newStockTimeLines);
    }

    private List<NewStockEventTimeLineDTO> buildNewStockTimeLines(List<NewStockEventTimeLineDTO> newStockTimeLines) {
        if (newStockTimeLines.isEmpty()) {
            return newStockTimeLines;
        }
        return newStockTimeLines.stream().map(it -> {
            NewStockEventTimeLineDTO newStockEventTimeLineDTO = new NewStockEventTimeLineDTO();
            newStockEventTimeLineDTO.setStockCode(it.getStockCode());
            newStockEventTimeLineDTO.setStockName(it.getStockName());
            newStockEventTimeLineDTO.setApplyTime(getUnixByFormatDate(it.getApplyTime()));
            newStockEventTimeLineDTO.setApplyStartTime(getUnixByFormatDate(it.getApplyStartTime()));
            newStockEventTimeLineDTO.setApplyEndTime(getUnixByFormatDate(it.getApplyEndTime()));
            newStockEventTimeLineDTO.setSetPriceTime(getUnixByFormatDate(it.getSetPriceTime()));
            newStockEventTimeLineDTO.setAnnouncedTime(getUnixByFormatDate(it.getAnnouncedTime()));
            newStockEventTimeLineDTO.setMarketTime(getUnixByFormatDate(it.getMarketTime()));
            newStockEventTimeLineDTO.setEndTime(getUnixByFormatDate(it.getEndTime()));
            return newStockEventTimeLineDTO;
        }).collect(Collectors.toList());
    }

    private Long getUnixByFormatDate(Long formatDate) {
        if (formatDate == null) {
            return null;
        }
        return DateUtils.parseDate(formatDate).getTime();
    }

    @Override
    public List<Xnhks0503> listXnhks0503(
        List<String> codes
    ) {
        return xnhks0503Mapper.selectLastedNewStock(codes);
    }

    @Override
    public ResultT<List<Xnhks0112>> listDividendInfoByF018D(List<String> codes, Long starTime, Long endTime) {
        LambdaQueryWrapper<Xnhks0112> xnhks0112Wrapper = Wrappers.<Xnhks0112>lambdaQuery()
                .in(CollectionUtils.isNotEmpty(codes), Xnhks0112::getSeccode, codes)
                .ge(Xnhks0112::getF018d,starTime)
                .le(Xnhks0112::getF018d,endTime);

        return ResultT.success(xnhks0112Mapper.selectList(xnhks0112Wrapper));
    }

}
