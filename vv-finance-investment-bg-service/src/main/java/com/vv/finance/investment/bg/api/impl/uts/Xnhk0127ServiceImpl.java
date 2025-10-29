package com.vv.finance.investment.bg.api.impl.uts;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.investment.bg.api.uts.Xnhk0127Service;
import com.vv.finance.investment.bg.dto.uts.resp.DividendAmountInfoResp;
import com.vv.finance.investment.bg.dto.uts.resp.RightsIssueEventInfoDTO;
import com.vv.finance.investment.bg.dto.uts.resp.Xnhk0127DTO;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0127Mapper;
import com.vv.finance.investment.bg.utils.LongDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class Xnhk0127ServiceImpl implements Xnhk0127Service {

    @Resource
    private Xnhk0127Mapper xnhk0127Mapper;

    private static final String CD = "CD";
    private static final String SD = "SD";

    @Override
    public List<DividendAmountInfoResp> queryDividendStock(Long queryDate) {

        List<Xnhk0127> filterList = this.queryDividendStockEntityList(queryDate);

        List<DividendAmountInfoResp> list = new ArrayList<>();
        filterList.stream().forEach(Xnhk0127 -> {
            DividendAmountInfoResp dividendAmountInfoResp = new DividendAmountInfoResp();
            dividendAmountInfoResp.setStockCode(Xnhk0127.getSeccode());
            dividendAmountInfoResp.setDividendAmount(Xnhk0127.getF005n());
            list.add(dividendAmountInfoResp);
        });
        return list;
    }

    /**
     * 获取供股信息
     *
     * @param stockCode   股票代码
     * @param exRightDate 除权日
     * @param type 除权类型
     * @return 供股信息
     */
    @Override
    public List<RightsIssueEventInfoDTO> getRightsIssueInfo(String stockCode, LocalDate exRightDate, String type) {
        List<RightsIssueEventInfoDTO> dtos = new LinkedList<>();
        LambdaQueryWrapper<Xnhk0127> wrapper = new LambdaQueryWrapper<>();
        List<Xnhk0127> list = xnhk0127Mapper.selectList(wrapper.eq(Xnhk0127::getSeccode, stockCode).eq(Xnhk0127::getF003d, exRightDate).in(Xnhk0127::getF007n, Arrays.asList(0, 1, 2)).eq(Xnhk0127::getF002v, type));
        for (Xnhk0127 xnhk0127 : list) {
            dtos.add(new RightsIssueEventInfoDTO().setExRightEvent(xnhk0127.getF006v()).setExRightDate(xnhk0127.getF003d()).setExRightType(xnhk0127.getF002v()));
        }
        return dtos;
    }

    public List<Xnhk0127> queryDividendStockEntityList(Long queryDate) {
        List<Xnhk0127> xnHk0127List = xnhk0127Mapper.queryDividendStock(queryDate);
        if (CollectionUtils.isEmpty(xnHk0127List)) {
            log.warn("查询指定日期每股派息信息为空.queryDate={}", queryDate);
            return new ArrayList<>();
        }

        List<Xnhk0127> list = xnHk0127List.stream()
                .filter(Objects::nonNull)
                .filter(value -> Objects.nonNull(value.getF007n()) && value.getF007n().equals(1))
                .filter(value -> Objects.nonNull(value.getF002v()))
                .filter(value -> Objects.nonNull(value.getF005n()))
                .filter(value -> (CD.equalsIgnoreCase(value.getF002v()) || SD.equalsIgnoreCase(value.getF002v())))
                .collect(Collectors.toList());

        return list;
    }

    @Override
   public List<Xnhk0127> getAllXrStockInfo(LocalDate queryDate){
//        20010514
        //CD=股息，SD=特别股息
        List<Xnhk0127> list1 = this.queryDividendStockEntityList(LongDateUtil.getLongDate(queryDate));
        LambdaQueryWrapper<Xnhk0127> wrapper = Wrappers.lambdaQuery();

        //OD=其他派送，BS=送股，BW=红利股权证，SS=拆股，SC=并股，RS=供股，OO=公开售股
        List<Xnhk0127> list2 = xnhk0127Mapper.selectList(wrapper.eq(Xnhk0127::getF003d, queryDate)
                .in(Xnhk0127::getF007n, Arrays.asList(0, 1, 2))
                .notIn(Xnhk0127::getF002v,Xnhk0127.DIVIDEND_TYPE_LIST));
        list1.addAll(list2);
        return list1;
    }

    @Override
    public List<Xnhk0127> getErrorXrRecords() {
        return xnhk0127Mapper.selectList(
                new LambdaQueryWrapper<Xnhk0127>().gt(Xnhk0127::getF003d, LocalDate.now())
                        .like(Xnhk0127::getF002v, ","));
    }

    @Override
    public List<Xnhk0127DTO> getMissedDividendRecords(Integer days) {
        return xnhk0127Mapper.getMissedDividendRecords(days);
    }

    @Override
    public List<Xnhk0127DTO> getErrorDividendRecords() {
        return xnhk0127Mapper.getErrorDividendRecords();
    }
}