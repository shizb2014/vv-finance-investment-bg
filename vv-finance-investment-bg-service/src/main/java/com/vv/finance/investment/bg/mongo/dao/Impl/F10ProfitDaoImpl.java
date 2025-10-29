package com.vv.finance.investment.bg.mongo.dao.Impl;

import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.enums.TableIdEnum;
import com.vv.finance.investment.bg.mongo.dao.F10ProfitDao;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @ClassName F10ProfitDaoImpl
 * @Deacription 利润
 * @Author lh.sz
 * @Date 2021年07月24日 11:10
 **/
@Service
public class F10ProfitDaoImpl implements F10ProfitDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public <T> List<T> listProfitEachCodeAndType(Class<T> clazz, String collectionName) {
        GroupOperation group = Aggregation.group("stockCode", "reportType");
        Field[] fields = clazz.getDeclaredFields();
        Field[] declaredFields = clazz.getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            if (!"serialVersionUID".equals(field.getName())) {
                group = group.first(field.getName()).as(field.getName());
            }
        }
        for (Field field : declaredFields) {
            if (!"serialVersionUID".equals(field.getName())) {
                group = group.first(field.getName()).as(field.getName());
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.sort(Sort.Direction.DESC, "releaseTimestamp"), group);
        return mongoTemplate.aggregate(aggregation, collectionName, clazz).getMappedResults();
    }

    @Override
    public F10PageResp<F10FinProfitEntity> pageFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
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
        long count = mongoTemplate.count(query, F10FinProfitEntity.class);
        F10PageResp<F10FinProfitEntity> f10PageResp = F10PageResp.<F10FinProfitEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10FinProfitEntity> entityList = mongoTemplate.find(pageRequest, F10FinProfitEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10NoFinProfitEntity> pageNonFinancial(F10PageReq<F10CommonRequest> requestPageReq) {
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

        long count = mongoTemplate.count(query, F10NoFinProfitEntity.class);
        F10PageResp<F10NoFinProfitEntity> f10PageResp = F10PageResp.<F10NoFinProfitEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10NoFinProfitEntity> entityList = mongoTemplate.find(pageRequest, F10NoFinProfitEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }

    @Override
    public F10PageResp<F10InsureProfitEntity> pageInsurance(F10PageReq<F10CommonRequest> requestPageReq) {
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
        long count = mongoTemplate.count(query, F10InsureProfitEntity.class);
        F10PageResp<F10InsureProfitEntity> f10PageResp = F10PageResp.<F10InsureProfitEntity>builder()
                .total(count).currentPage(requestPageReq.getCurrentPage())
                .pageSize(requestPageReq.getPageSize()).build();
        if (count == 0) {
            return f10PageResp;
        }
        Query pageRequest = query.
                with(PageRequest.of(requestPageReq.getCurrentPage(), requestPageReq.getPageSize(), Sort.by(Sort.Direction.DESC, "endTimestamp")));

        List<F10InsureProfitEntity> entityList = mongoTemplate.find(pageRequest, F10InsureProfitEntity.class);
        f10PageResp.setRecord(entityList);
        return f10PageResp;
    }
}
