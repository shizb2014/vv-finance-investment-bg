package com.vv.finance.investment.bg.mongo.dao.Impl;

import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.enums.TableIdEnum;
import com.vv.finance.investment.bg.mongo.dao.F10AssetsLiabilitiesDao;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesNonFinancialEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName F10AssetsLiabilitiesDaoImpl
 * @Deacription 资产负债表
 * @Author lh.sz
 * @Date 2021年07月24日 11:10
 **/
@Service
public class F10AssetsLiabilitiesDaoImpl implements F10AssetsLiabilitiesDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public F10PageResp<F10AssetsLiabilitiesFinancialEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
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
        long count = mongoTemplate.count(query, F10AssetsLiabilitiesFinancialEntity.class);
        F10PageResp<F10AssetsLiabilitiesFinancialEntity> f10PageResp = F10PageResp.<F10AssetsLiabilitiesFinancialEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10AssetsLiabilitiesFinancialEntity> entityList = mongoTemplate.find(pageRequest, F10AssetsLiabilitiesFinancialEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10AssetsLiabilitiesNonFinancialEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
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
        long count = mongoTemplate.count(query, F10AssetsLiabilitiesNonFinancialEntity.class);
        F10PageResp<F10AssetsLiabilitiesNonFinancialEntity> f10PageResp = F10PageResp.<F10AssetsLiabilitiesNonFinancialEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10AssetsLiabilitiesNonFinancialEntity> entityList = mongoTemplate.find(pageRequest, F10AssetsLiabilitiesNonFinancialEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10AssetsLiabilitiesInsuranceEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
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
        long count = mongoTemplate.count(query, F10AssetsLiabilitiesInsuranceEntity.class);
        F10PageResp<F10AssetsLiabilitiesInsuranceEntity> f10PageResp = F10PageResp.<F10AssetsLiabilitiesInsuranceEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10AssetsLiabilitiesInsuranceEntity> entityList = mongoTemplate.find(pageRequest, F10AssetsLiabilitiesInsuranceEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }
}
