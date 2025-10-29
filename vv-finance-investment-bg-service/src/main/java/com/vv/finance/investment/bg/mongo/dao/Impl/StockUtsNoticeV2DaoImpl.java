package com.vv.finance.investment.bg.mongo.dao.Impl;

import cn.hutool.core.collection.CollUtil;
import com.vv.finance.investment.bg.constants.StockUtsNoticeEnum;
import com.vv.finance.investment.bg.dto.uts.resp.NoticeMongoPageResp;
import com.vv.finance.investment.bg.dto.uts.resp.StockUtsNoticeResp;
import com.vv.finance.investment.bg.mongo.dao.StockUtsNoticeV2Dao;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntityV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenyu
 * @date 2021/7/19 16:41
 */
@Slf4j
@Component
public class StockUtsNoticeV2DaoImpl implements StockUtsNoticeV2Dao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Integer saveOrUpdate(StockUtsNoticeEntityV2 stockUtsNoticeEntityV2,List<Integer> flags) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lineId").is(stockUtsNoticeEntityV2.getLineId()));
        query.addCriteria(Criteria.where("rawPath").is(stockUtsNoticeEntityV2.getRawPath()));
        boolean pathExists = mongoTemplate.exists(query, StockUtsNoticeEntityV2.class);
        log.info("=========公告信息保存mongodb lineId {} rawPath :{} rawPath是否已保存 {} ",stockUtsNoticeEntityV2.getLineId(),stockUtsNoticeEntityV2.getRawPath(),pathExists);
        if (!pathExists) {
             StockUtsNoticeEntityV2 save = mongoTemplate.save(stockUtsNoticeEntityV2);
            int result = save.getId() != null ? 1 : 0;
            flags.add(result);
            return result;
        }
        query.addCriteria(Criteria.where("categoryId").is(stockUtsNoticeEntityV2.getCategoryId()));
        boolean cateExists = mongoTemplate.exists(query, StockUtsNoticeEntityV2.class);
        log.info("=========公告信息保存mongodb lineId :{} categoryId :{} cateExists是否已保存 :{}",stockUtsNoticeEntityV2.getLineId(),stockUtsNoticeEntityV2.getCategoryId(),cateExists);

        if (!cateExists) {
            StockUtsNoticeEntityV2 save = mongoTemplate.save(stockUtsNoticeEntityV2);
            int result = save.getId() != null ? 1 : 0;
            flags.add(result);
            return result;
        }
        return 0;
    }

    @Override
    public NoticeMongoPageResp queryNotice(List<String> types, Integer type, String code, Integer pageSize, Integer currentPage) {
        Query query = new Query();
        if (type != StockUtsNoticeEnum.ALL.getCode()) {
            if (type == StockUtsNoticeEnum.OTHER.getCode()) {
                query.addCriteria(Criteria.where("categoryId").nin(types));
            } else {
                query.addCriteria(Criteria.where("categoryId").in(types));
            }
        }
        //由于数据看保存的是int类型故，造模拟股票数据时-t.hk适配成0000，增删改查同步适配
        query.addCriteria(Criteria.where("stockCode").is(Integer.valueOf(code.replace("-t","0000").replace("-",""))));

        query.addCriteria(Criteria.where("language").is(2));


        long count = mongoTemplate.count(query, StockUtsNoticeEntityV2.class);

        if (count == 0) {
            return NoticeMongoPageResp.builder().total(count).build();
        }

        //        query.with(PageRequest.of(currentPage,pageSize,Sort.by("dateLine")))

        List<StockUtsNoticeEntityV2> noticeEntities = mongoTemplate.find(query, StockUtsNoticeEntityV2.class);

        List<StockUtsNoticeResp> collect = noticeEntities.stream().map(item -> {
            StockUtsNoticeResp stockUtsNoticeResp = new StockUtsNoticeResp();
            BeanUtils.copyProperties(item, stockUtsNoticeResp);
            return stockUtsNoticeResp;
        }).collect(Collectors.toList());

        return NoticeMongoPageResp.builder().total(count).data(collect).build();
    }
    /**
     * 通过目录获取目录下的所有文件
     * @param dirs 目录
     * @return
     */
    @Override
    public List<StockUtsNoticeEntityV2> getNoticByDirs(String dirs) {
        Query query = new Query();
        query.addCriteria(Criteria.where("dirs").is(dirs));
        List<StockUtsNoticeEntityV2> noticeResps = mongoTemplate.find(query, StockUtsNoticeEntityV2.class);
        return noticeResps;
    }
    @Override
    public void delByStockCode(String stockCode) {
        //由于数据看保存的是int类型故，造模拟股票数据时-t.hk适配成0000，增删改查同步适配
        mongoTemplate.remove(Query.query(Criteria.where("stockCode").is(Integer.valueOf(stockCode.replace("-t","0000").replace(".hk", "").replace("-","")))), StockUtsNoticeEntityV2.class);
    }
    /**
     * 变更公告股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    @Override
    public void upNoticeStockCode(String sourceCode, String targetCode) {
        //由于数据看保存的是int类型故，造模拟股票数据时-t.hk适配成0000，增删改查同步适配
        Query query = Query.query(Criteria.where("stockCode").is(Integer.valueOf(sourceCode.replace("-t","0000").replace(".hk", "").replace("-",""))));
        List<StockUtsNoticeEntityV2> noticeEntityV2s = mongoTemplate.find(query, StockUtsNoticeEntityV2.class);
        if (CollUtil.isNotEmpty(noticeEntityV2s)) {
            noticeEntityV2s.forEach(notice->notice.setStockCode(Integer.valueOf(targetCode.replace("-t","0000").replace(".hk", "").replace("-",""))));
            mongoTemplate.remove(query, StockUtsNoticeEntityV2.class);
            mongoTemplate.insertAll(noticeEntityV2s);
        }
    }

    /**
     * 新增模拟股票公告数据
     *
     * @param simulateCode 模拟股票code
     */
    @Override
    public void saveSimulateNoticeInfo(String simulateCode) {
        //获取真实股票code
        String realCode = simulateCode.replace("-t","" );
        Query query = Query.query(Criteria.where("stockCode").is(Integer.valueOf(realCode.replace(".hk", "")))).with(Sort.by(Sort.Order.desc("lineId"))).limit(20);;
        List<StockUtsNoticeEntityV2> noticeEntityV2s = mongoTemplate.find(query, StockUtsNoticeEntityV2.class);
        if (CollUtil.isNotEmpty(noticeEntityV2s)) {
            //由于数据看保存的是int类型故，造模拟股票数据时-t.hk适配成0000，增删改查同步适配
            noticeEntityV2s.forEach(notice->notice.setStockCode(Integer.valueOf(simulateCode.replace("-t.hk", "0000"))));
            mongoTemplate.insertAll(noticeEntityV2s);
        }
    }
}
