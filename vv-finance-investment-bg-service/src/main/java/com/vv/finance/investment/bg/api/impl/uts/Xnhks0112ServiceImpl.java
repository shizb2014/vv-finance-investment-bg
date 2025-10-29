package com.vv.finance.investment.bg.api.impl.uts;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.investment.bg.api.uts.Xnhks0112Service;
import com.vv.finance.investment.bg.dto.uts.req.DividendDateInfoReq;
import com.vv.finance.investment.bg.dto.uts.resp.DividendDateInfoResp;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0112Mapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author:maling
 * @Date:2023/6/14
 * @Description:
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class Xnhks0112ServiceImpl implements Xnhks0112Service {

    @Resource
    private Xnhks0112Mapper xnhks0112Mapper;

    @Override
    public List<DividendDateInfoResp> queryDividendDateInfo(DividendDateInfoReq req) {

        LambdaQueryWrapper<Xnhks0112> xnhks0112Wrapper = Wrappers.<Xnhks0112>lambdaQuery()
                .in(CollectionUtils.isNotEmpty(req.getStockCodeList()), Xnhks0112::getSeccode, req.getStockCodeList())
                .ge(Xnhks0112::getF018d, req.getStartDate())
                .le(Xnhks0112::getF018d, req.getEndDate());
        List<Xnhks0112> respList = xnhks0112Mapper.selectList(xnhks0112Wrapper);

        List<DividendDateInfoResp> list = new ArrayList<>();
        respList.stream().forEach(xnhks0112 -> {
            DividendDateInfoResp dividendDateInfoResp = new DividendDateInfoResp();
            dividendDateInfoResp.setStockCode(xnhks0112.getSeccode());
            dividendDateInfoResp.setDividendDate(getDividendDate(xnhks0112));
            dividendDateInfoResp.setDividendRemark(xnhks0112.getF006v());
            list.add(dividendDateInfoResp);
        });
        return list;
    }


    //派息日
    private LocalDate getDividendDate(Xnhks0112 xnhks0112) {
        if (Objects.nonNull(xnhks0112)) {
            long dividendDate = xnhks0112.getF010d() == null ? (xnhks0112.getF011d() == null ? (xnhks0112.getF012d() == null ?
                    (xnhks0112.getF013d() == null ? (xnhks0112.getF014d() == null ? (xnhks0112.getF015d() == null ?
                            (xnhks0112.getF017d() == null ? 1L : xnhks0112.getF017d()) : xnhks0112.getF015d()) : xnhks0112.getF014d())
                            : xnhks0112.getF013d()) : xnhks0112.getF012d()) : xnhks0112.getF011d()) : xnhks0112.getF010d();
            if (dividendDate != 1L) {
                return LocalDate.parse(String.valueOf(dividendDate), DateTimeFormatter.BASIC_ISO_DATE);
            }
        }
        log.info("查询股票的派息日期信息,派息日期查询为空.xnhks0112={}", xnhks0112);
        return null;
    }

    @Override
    public List<Xnhks0112> getXnhk0112sIncludeSS(Date startDate, Date endDate) {
        return xnhks0112Mapper.getXnhk0112sIncludeSS(startDate, endDate);
    }

    @Override
    public List<Xnhks0112> getXnhk0112sNotIncludeSS(Date startDate, Date endDate) {
        return xnhks0112Mapper.getXnhk0112sNotIncludeSS(startDate, endDate);
    }
}