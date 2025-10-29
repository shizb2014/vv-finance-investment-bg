package com.vv.finance.investment.bg.mongo.dao.Impl;

import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.enums.TableIdEnum;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.model.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/7/23 15:09
 */
@Service
public class F10KeyFiguresDaoImpl implements F10KeyFiguresDao {
    @Resource
    private MongoTemplate mongoTemplate;

    private static final String REPORT_TYPE = "reportType";

    @Override
    public F10PageResp<F10KeyFiguresFinancialEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
                }
            } else {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).regex(TableIdEnum.getByCode(requestPageReq.getParams().getReportId()).getDesc()));
            }
        } else {
            if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType()) && null != requestPageReq.getParams().getReportTime()) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType())
                        .and("endTimestamp").is(requestPageReq.getParams().getReportTime()));
            } else if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType())) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType()));
            } else {
                if (StringUtils.isNotEmpty(requestPageReq.getParams().getEndDate())) {
                    query = Query.query(Criteria.where("endDate").is(requestPageReq.getParams().getEndDate()));
                } else {
                    query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                }
            }
        }
        long count = mongoTemplate.count(query, F10KeyFiguresFinancialEntity.class);
        F10PageResp<F10KeyFiguresFinancialEntity> f10PageResp = F10PageResp.<F10KeyFiguresFinancialEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10KeyFiguresFinancialEntity> f10KeyFiguresFinancialEntities = mongoTemplate.find(pageRequest, F10KeyFiguresFinancialEntity.class);
        f10PageResp.setRecord(f10KeyFiguresFinancialEntities);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10KeyFiguresNonFinancialEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
                }
            } else {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).regex(TableIdEnum.getByCode(requestPageReq.getParams().getReportId()).getDesc()));
            }
        } else {
            if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType()) && null != requestPageReq.getParams().getReportTime()) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType())
                        .and("endTimestamp").is(requestPageReq.getParams().getReportTime()));
            } else if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType())) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType()));
            } else {
                if (StringUtils.isNotEmpty(requestPageReq.getParams().getEndDate())) {
                    query = Query.query(Criteria.where("endDate").is(requestPageReq.getParams().getEndDate()));
                } else {
                    query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                }
            }
        }
        long count = mongoTemplate.count(query, F10KeyFiguresNonFinancialEntity.class);
        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = F10PageResp.<F10KeyFiguresNonFinancialEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.with(Sort.by(Sort.Direction.DESC, "endTimestamp")).
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize()));

        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresFinancialEntities = mongoTemplate.find(pageRequest, F10KeyFiguresNonFinancialEntity.class);
        f10PageResp.setRecord(f10KeyFiguresFinancialEntities);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10KeyFiguresInsuranceEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq) {
        Query query;
        if (requestPageReq.getParams().getReportId() > 0) {
            if (requestPageReq.getParams().getReportId() == 1) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                if (requestPageReq.getParams().isFilterPq()) {
                    query.addCriteria(Criteria.where("reportType").nin(ReportTypeEnum.unResolveTypeList()));
                }
            } else {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).regex(TableIdEnum.getByCode(requestPageReq.getParams().getReportId()).getDesc()));
            }
        } else {
            if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType()) && null != requestPageReq.getParams().getReportTime()) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType())
                        .and("endTimestamp").is(requestPageReq.getParams().getReportTime()));
            } else if (StringUtils.isNotEmpty(requestPageReq.getParams().getReportType())) {
                query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode())
                        .and(REPORT_TYPE).is(requestPageReq.getParams().getReportType()));
            } else {
                if (StringUtils.isNotEmpty(requestPageReq.getParams().getEndDate())) {
                    query = Query.query(Criteria.where("endDate").is(requestPageReq.getParams().getEndDate()));
                } else {
                    query = Query.query(Criteria.where("stockCode").is(requestPageReq.getParams().getStockCode()));
                }
            }
        }
        long count = mongoTemplate.count(query, F10KeyFiguresInsuranceEntity.class);
        F10PageResp<F10KeyFiguresInsuranceEntity> f10PageResp = F10PageResp.<F10KeyFiguresInsuranceEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.with(Sort.by(Sort.Direction.DESC, "endTimestamp")).
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize()));

        List<F10KeyFiguresInsuranceEntity> f10KeyFiguresFinancialEntities = mongoTemplate.find(pageRequest, F10KeyFiguresInsuranceEntity.class);
        f10PageResp.setRecord(f10KeyFiguresFinancialEntities);
        return f10PageResp;
    }

    @Override
    public List<F10NoFinProfitEntity> listNonFinancial(String id, int size, Long startTime) {
        Query query = new Query();
        if(StringUtils.isNotBlank(id)){
            ObjectId objectId = new ObjectId(id);
            query.addCriteria(Criteria.where("_id").gt(objectId));
        }
        if(ObjectUtils.isNotEmpty(startTime)){
            query.addCriteria(Criteria.where("startTimestamp").gte(startTime));
        }
        query.with(Sort.by(Sort.Direction.ASC, "_id")); // 按_id降序排列
        query.limit(size); // 限制返回的文档数量
        return mongoTemplate.find(query, F10NoFinProfitEntity.class);
    }

    @Override
    public List<F10FinProfitEntity> listFinancial(String id, int size, Long startTime) {
        Query query = new Query();
        if(StringUtils.isNotBlank(id)){
            ObjectId objectId = new ObjectId(id);
            query.addCriteria(Criteria.where("_id").gt(objectId));
        }
        if(ObjectUtils.isNotEmpty(startTime)){
            query.addCriteria(Criteria.where("startTimestamp").gte(startTime));
        }
        query.with(Sort.by(Sort.Direction.ASC, "_id")); // 按_id降序排列
        query.limit(size); // 限制返回的文档数量
        return mongoTemplate.find(query, F10FinProfitEntity.class);
    }

    @Override
    public List<F10InsureProfitEntity> listInsurance(String id, int size, Long startTime) {
        Query query = new Query();
        if(StringUtils.isNotBlank(id)){
            ObjectId objectId = new ObjectId(id);
            query.addCriteria(Criteria.where("_id").gt(objectId));
        }
        if(ObjectUtils.isNotEmpty(startTime)){
            query.addCriteria(Criteria.where("startTimestamp").gte(startTime));
        }
        query.with(Sort.by(Sort.Direction.ASC, "_id")); // 按_id降序排列
        query.limit(size); // 限制返回的文档数量
        return mongoTemplate.find(query, F10InsureProfitEntity.class);
    }
}
