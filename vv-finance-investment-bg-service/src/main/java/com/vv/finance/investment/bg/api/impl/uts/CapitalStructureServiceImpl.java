package com.vv.finance.investment.bg.api.impl.uts;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.uts.ICapitalStructureService;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalChange;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalStatistics;
import com.vv.finance.investment.bg.entity.f10.enums.F10CapitalSharesTypeEnum;
import com.vv.finance.investment.bg.entity.f10.enums.F10CapitalStatisticsType;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0114Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0605Mapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/2 10:58
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class CapitalStructureServiceImpl implements ICapitalStructureService {

    @Resource
    private Xnhk0605Mapper xnhk0605Mapper;
    @Resource
    private Xnhk0114Mapper xnhk0114Mapper;

    @Override
    public CapitalStatistics getCapitalStructure(String stockCode) {
        // if (!checkCode(stockCode)) {
        //     return null;
        // }
        CapitalStatistics capitalStructure = xnhk0605Mapper.getCapitalStructure(stockCode);
        if (ObjectUtils.isNotEmpty(capitalStructure)) {
            try {
                capitalStructure.setUpdateDate(DateUtils.formatDate(DateUtils.parseDate(capitalStructure.getUpdateDate(), "yyyyMMdd"), "yyyy/MM/dd"));
                capitalStructure.setType(F10CapitalStatisticsType.getValue(capitalStructure.getType()));
                String str = xnhk0114Mapper.lastF002D(stockCode);
                capitalStructure.setLastTime(StringUtils.isBlank(str) ? str : DateUtils.formatDate(DateUtils.parseDate(str, "yyyyMMdd"), "yyyy/MM/dd"));
            } catch (ParseException e) {
                log.error("date err");
            }
        }
        return capitalStructure;
    }

    @Override
    public PageDomain<CapitalChange> pageCapitalChange(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        // if (!checkCode(stockCode)) {
        //     return null;
        // }
        Page<CapitalChange> capitalChangePage = new Page<>();
        capitalChangePage.setCurrent(currentPage);
        capitalChangePage.setSize(pageSize);
        Page<CapitalChange> pageResult = xnhk0114Mapper.pageCapitalChange(capitalChangePage, stockCode);

        PageDomain<CapitalChange> pageDomain = new PageDomain<>();
        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        List<CapitalChange> records = pageResult.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.stream().forEach(re -> {
                try {
                    re.setChangeDate(DateUtils.formatDate(DateUtils.parseDate(re.getChangeDate(), "yyyyMMdd"), "yyyy/MM/dd"));
                    re.setReleaseDate(DateUtils.formatDate(DateUtils.parseDate(re.getReleaseDate(), "yyyyMMdd"), "yyyy/MM/dd"));
                    re.setType(F10CapitalSharesTypeEnum.getValue(re.getType()));
                } catch (ParseException e) {
                    log.error("date err");
                }
            });
        }
        pageDomain.setRecords(records);
        return pageDomain;
    }

    private Boolean checkCode(String code) {
        return code.matches("^\\d{5}.hk");
    }
}
