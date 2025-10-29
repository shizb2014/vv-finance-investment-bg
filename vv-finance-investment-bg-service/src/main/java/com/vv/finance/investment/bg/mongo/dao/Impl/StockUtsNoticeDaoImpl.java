//package com.vv.finance.investment.bg.mongo.dao.Impl;
//
//import com.vv.finance.investment.bg.constants.StockUtsNoticeEnum;
//import com.vv.finance.investment.bg.dto.uts.resp.NoticeMongoPageResp;
//import com.vv.finance.investment.bg.dto.uts.resp.StockUtsNoticeListResp;
//import com.vv.finance.investment.bg.dto.uts.resp.StockUtsNoticeResp;
//import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
//import com.vv.finance.investment.bg.mongo.dao.StockUtsNoticeDao;
//import org.springframework.beans.BeanUtils;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author chenyu
// * @date 2021/7/19 16:41
// */
//@Component
//public class StockUtsNoticeDaoImpl implements StockUtsNoticeDao {
//
//    @Resource
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Integer saveOrUpdate(StockUtsNoticeEntity stockUtsNoticeEntity) {
////        Assert.isNull(stockUtsNoticeEntity,"stockUtsNotice can't be null");
//        Query query = new Query();
//        query.addCriteria(Criteria.where("lineId").is(stockUtsNoticeEntity.getLineId()));
//        query.addCriteria(Criteria.where("rawPath").is(stockUtsNoticeEntity.getRawPath()));
//        boolean pathExists = mongoTemplate.exists(query, StockUtsNoticeEntity.class);
//        if (!pathExists){
//            mongoTemplate.save(stockUtsNoticeEntity);
//            return 1;
//        }
//        query.addCriteria(Criteria.where("categoryId").is(stockUtsNoticeEntity.getCategoryId()));
//        boolean cateExists = mongoTemplate.exists(query, StockUtsNoticeEntity.class);
//        if (!cateExists){
//            mongoTemplate.save(stockUtsNoticeEntity);
//            return 0;
//        }
//        return 0;
//    }
//
//    @Override
//    public NoticeMongoPageResp queryNotice(List<String> types, Integer type, String code, Integer pageSize, Integer currentPage) {
//        Query query = new Query();
//        if (type!=StockUtsNoticeEnum.ALL.getCode()){
//            if (type == StockUtsNoticeEnum.OTHER.getCode()){
//                query.addCriteria(Criteria.where("categoryId").nin(types));
//            }else {
//                query.addCriteria(Criteria.where("categoryId").in(types));
//            }
//        }
//        query.addCriteria(Criteria.where("stockCode").is(Integer.valueOf(code)));
//
//        query.addCriteria(Criteria.where("language").is(2));
//
//
//        long count = mongoTemplate.count(query, StockUtsNoticeEntity.class);
//
//        if (count ==0){
//            return NoticeMongoPageResp.builder().total(count).build();
//        }
//
//        //        query.with(PageRequest.of(currentPage,pageSize,Sort.by("dateLine")))
//
//        List<StockUtsNoticeEntity> noticeEntities = mongoTemplate.find(query, StockUtsNoticeEntity.class);
//
//        List<StockUtsNoticeResp> collect = noticeEntities.stream().map(item -> {
//            StockUtsNoticeResp stockUtsNoticeResp = new StockUtsNoticeResp();
//            BeanUtils.copyProperties(item, stockUtsNoticeResp);
//            return stockUtsNoticeResp;
//        }).collect(Collectors.toList());
//
//        return NoticeMongoPageResp.builder().total(count).data(collect).build();
//    }
//}
