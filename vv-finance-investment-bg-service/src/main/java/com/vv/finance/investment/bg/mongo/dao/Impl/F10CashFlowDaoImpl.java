package com.vv.finance.investment.bg.mongo.dao.Impl;

import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.enums.TableIdEnum;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName F10CashFlowDaoImpl
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2021年07月24日 11:09
 **/
@Component
public class F10CashFlowDaoImpl implements F10CashFlowDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public F10PageResp<F10CashFlowEntity> pageCashFlow(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
                } else {
                    query.addCriteria(Criteria.where("reportType").ne("P"));
                }
            } else {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and("reportType").regex(TableIdEnum.getByCode(requestPageReq.getParams().getReportId()).getDesc()));
            }
        } else {
            if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType()) && null != requestPageReq.getParams().getReportTime()) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and("reportType").is(requestPageReq.getParams().getReportType())
                        .and("endTimestamp").is(requestPageReq.getParams().getReportTime()));
            } else if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType())) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and("reportType").is(requestPageReq.getParams().getReportType()));
            } else {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
            }
        }
        long count = mongoTemplate.count(query, F10CashFlowEntity.class);
        F10PageResp<F10CashFlowEntity> f10PageResp = F10PageResp.<F10CashFlowEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10CashFlowEntity> entityList = mongoTemplate.find(pageRequest, F10CashFlowEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }
}
