package com.vv.finance.investment.bg.api.impl.information;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.BaseEnum;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.information.InformationManagementApi;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.PublishTerminalEnum;
import com.vv.finance.investment.bg.constants.TopCategoryMap;
import com.vv.finance.investment.bg.entity.information.PublishDto;
import com.vv.finance.investment.bg.entity.information.PublishLogDto;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.information.StockNewsListReq;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import com.vv.finance.log.api.BusinessOptLogApi;
import com.vv.finance.log.dto.BusinessOptLogDto;
import com.vv.finance.log.dto.QueryLogReq;
import com.vv.finance.log.dto.QueryLogResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/9/17 14:44
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class InformationManagementApiImpl implements InformationManagementApi {
    private final IStockNewsService stockNewsService;
    @DubboReference(group = "${dubbo.log.group:log}", registry = "logservice")
    private BusinessOptLogApi businessOptLogApi;
    List<String> types=Lists.newArrayList("新股","IPO动态");
    @Override
    public ResultT<SimplePageResp<StockNewsEntity>> list(StockNewsListReq stockNewsListReq) {
        StockNewsEntity stockNewsEntity = new StockNewsEntity();
        BeanUtils.copyProperties(stockNewsListReq, stockNewsEntity);
        stockNewsEntity.setPublishStatus(BaseEnum.getByCode(stockNewsListReq.getPublishStatus(),PublishStatusEnum.class));
        stockNewsEntity.setPublishTerminal(BaseEnum.getByCode(stockNewsListReq.getPublishTerminal(),PublishTerminalEnum.class));
        Long startDate = stockNewsListReq.getStartDate();
        Long endDate = stockNewsListReq.getEndDate();
        //stockNewsEntity.setDate(date == null ? null : Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate());
        Page<StockNewsEntity> page = new Page<>(stockNewsListReq.getCurrentPage(), stockNewsListReq.getPageSize());
        stockNewsEntity.setTopCategory(null);
        stockNewsEntity.setSecondCategory(null);
        stockNewsEntity.setKeyword(null);
        stockNewsEntity.setRelationStock(null);
        String topCategory = stockNewsListReq.getTopCategory();
        List<String> tables=Lists.newArrayList();

        if(StringUtil.isNotEmpty(topCategory)){
            tables=Lists.newArrayList(topCategory.split("#"));
        }
        String secondCategory = stockNewsListReq.getSecondCategory();
        List<String> typeReq=Lists.newArrayList();
        if(StringUtil.isNotEmpty(secondCategory)){
            typeReq.add(secondCategory);
        }
        LocalDate startLocalDate=null;
        LocalDate endLocalDate=null;
        if(startDate !=null){
            startLocalDate= LocalDateTimeUtil.getLocalDate(startDate, ZoneOffset.ofHours(8));
        }
        if(endDate != null){
            endLocalDate = LocalDateTimeUtil.getLocalDate(endDate,ZoneOffset.ofHours(8));
        }

        Page<StockNewsEntity> pageResult = stockNewsService.page(page, new UpdateWrapper<>(stockNewsEntity).
                in(!tables.isEmpty(),"top_category",tables).
                in(tables.size()>1&&typeReq.isEmpty(),"second_category",types).
                in(!typeReq.isEmpty(),"second_category",typeReq).
                ge(startLocalDate!=null,"date",startLocalDate).
                le(endLocalDate!=null,"date",endLocalDate).
                like(stockNewsListReq.getKeyword()!=null,"keyword",stockNewsListReq.getKeyword()).
                like(stockNewsListReq.getRelationStock()!=null,"relation_stock",stockNewsListReq.getRelationStock()).
                orderByDesc("date_time"));
        SimplePageResp<StockNewsEntity> simplePageResp = new SimplePageResp<>();
        BeanUtils.copyProperties(pageResult, simplePageResp);
        TopCategoryMap.setTopCategoryName(pageResult.getRecords());
        simplePageResp.setRecord(pageResult.getRecords());
        return ResultT.success(simplePageResp);
    }

    @Override
    public ResultT<StockNewsEntity> detail(Long id) {
        StockNewsEntity newsServiceById = stockNewsService.getById(id);
        return ResultT.success(newsServiceById);
    }

    @Override
    @Transactional
    public ResultT<Void> publish(PublishDto publishDto) {
        log.info("资讯发布或撤销发布操作 publishDto={}", publishDto);
        StockNewsEntity stockNewsEntity = stockNewsService.getById(publishDto.getId());
        if (stockNewsEntity == null) {
            return ResultT.fail("数据不存在", "数据不存在");
        }
        log.info("资讯发布或撤销发布操作原数据 stockNewsEntity={}", stockNewsEntity);
        StockNewsEntity updateById = new StockNewsEntity();
        updateById.setId(publishDto.getId());
        updateById.setRemark(publishDto.getRemark());
        PublishStatusEnum publishStatus = publishDto.getPublishStatus();
        PublishTerminalEnum publishTerminal = publishDto.getPublishTerminal();
        if (publishStatus == PublishStatusEnum.YES) {
            updateById.setPublishStatus(publishStatus);
            updateById.setPublishTerminal(publishTerminal);
        } else {
            if (publishTerminal == PublishTerminalEnum.PC_APP) {
                updateById.setPublishStatus(publishStatus);
                updateById.setPublishTerminal(publishTerminal);
            } else {
                if (stockNewsEntity.getPublishStatus() == PublishStatusEnum.YES) {
                    //原来发布两端
                    if (stockNewsEntity.getPublishTerminal() == PublishTerminalEnum.PC_APP) {
                        if (publishTerminal == PublishTerminalEnum.PC) {
                            updateById.setPublishTerminal(PublishTerminalEnum.APP);
                        } else {
                            updateById.setPublishTerminal(PublishTerminalEnum.PC);
                        }

                    }
                }
            }

        }

        stockNewsService.updateById(updateById);


        BusinessOptLogDto businessOptLogDto = new BusinessOptLogDto();
        businessOptLogDto.setBusinessId(publishDto.getId().toString());
        String opt = PublishStatusEnum.YES == publishStatus ? "发布" : "取消发布";
        businessOptLogDto.setOptType(opt);
        businessOptLogDto.setBusinessType(StockNewsEntity.class.getSimpleName());
        businessOptLogDto.setBusinessContent(JSON.toJSONString(publishDto));
        businessOptLogDto.setOptUserId(publishDto.getUserId());
        businessOptLogDto.setDeptId(publishDto.getDeptId());
        businessOptLogDto.setOptDate(System.currentTimeMillis());
        businessOptLogDto.setOptName(publishDto.getNickName());
        businessOptLogApi.saveLog(businessOptLogDto);
        return ResultT.success();
    }

    @Override
    public ResultT<List<PublishLogDto>> publishLog(Long id) {
        QueryLogReq queryLogReq = new QueryLogReq();
        queryLogReq.setBusinessId(id.toString());
        queryLogReq.setBusinessType(StockNewsEntity.class.getSimpleName());
        queryLogReq.setPage(0);
        queryLogReq.setSize(Integer.MAX_VALUE);
        QueryLogResp queryLogResp = businessOptLogApi.queryLog(queryLogReq);
        List<BusinessOptLogDto> businessOptLogList = queryLogResp.getBusinessOptLogList();

        if (CollUtil.isEmpty(businessOptLogList)) {
            return ResultT.success(Lists.newArrayList());
        }
        List<PublishLogDto> collect = businessOptLogList.stream().map(item -> {
            PublishLogDto publishLogDto = new PublishLogDto();
            publishLogDto.setOptUserId(item.getOptUserId());
            PublishDto publishDto = JSON.parseObject(item.getBusinessContent(), PublishDto.class);

            publishLogDto.setPublishStatus(publishDto.getPublishStatus());
            publishLogDto.setPublishTerminal(publishDto.getPublishTerminal());
            publishLogDto.setRemark(publishDto.getRemark());
            publishLogDto.setOptUserName(item.getOptName());
            publishLogDto.setDateTime(DateUtil.format(new Date(item.getOptDate()), "yyyy/MM/dd HH:mm:ss"));
            return publishLogDto;
        }).collect(Collectors.toList());
        return ResultT.success(collect);
    }
}
