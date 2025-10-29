package com.vv.finance.investment.bg.job.uts;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.notice.NoticeRepair;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.enums.NoticeJobTypeEnum;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.StockUtsNoticeV2Dao;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntityV2;
import com.vv.finance.investment.bg.utils.CollectUtils;
import com.vv.minio.starter.core.FileStorageManager;
import com.vv.minio.starter.core.MinioTemplate;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.i18nformatter.qual.I18nFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @ClassName: StockUtsNoticeJob
 * @Description: 股票公告job
 * @Author: chenyu
 * @Datetime: 2020/12/7   11:27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockUtsNoticeV2Job {

    private final HkIisNewsAttachmentHistoricMapper attachmentHistoricMapper;

    private final HkIisNewsAttachmentMapper attachmentMapper;

    private final HkIisNewsCateRefHistoricMapper cateRefHistoricMapper;

    private final HkIisNewsCateRefMapper cateRefMapper;

    private final HkIisNewsHeadlineHistoricMapper headlineHistoricMapper;

    private final HkIisNewsHeadlineMapper headlineMapper;

    private final HkIisNewsSecurityRefHistoricMapper securityRefHistoricMapper;

    private final HkIisNewsSecurityRefMapper securityRefMapper;

//    private final MinioTemplate minioTemplate;
    @Autowired
    private FileStorageManager fileStorageManager;

    private final StockUtsNoticeV2Dao stockUtsNoticeV2Dao;

    ExecutorService noticeExecutorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(4, 8, 0,TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000), new CustomizableThreadFactory("uts-notice")));

    ExecutorService putStreamExecutorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new CustomizableThreadFactory("stream-notice")));

    private final RedisClient redisClient;
    private String noticeItem="%s_%s_%s_%s";
    private String noticeRedisKey="bg:notice:/attachment/";
    @Resource
    private MongoTemplate mongoTemplate;

    //0 0/30 * ? * * 每30分钟同步一次
//    @SneakyThrows
//    @XxlJob(value = "updateTodayNoticeV2", cron = "0 0/30 * ? * * ", author = "罗浩", desc = "同步全量的公告（已弃用）")
//    public ReturnT<String> updateTodayNotice(String param) {
//        String date = param;
//        //读取文件
//        if (StringUtils.isEmpty(date)) {
//            date = DateUtil.format(new Date(), "yyyyMMdd");
//        }
//        String filePath = "/attachment/";
//        String result = filePath.concat(date).concat("/");
//        HashMap<String, String> fileMap = new HashMap<>();
//        // 把服务器中 /attachment/20220401/ 路径下的文件 绝对路径.xxx.PDF 和 文件名 返回 fileMap<路径，文件名>
//        listFile(result, fileMap);
//        log.info("=========当日公告 :{}", fileMap);
//        if (fileMap.isEmpty()) {
//            return ReturnT.SUCCESS;
//        }
//        //通过目录获取目录下已保存mongodb的所有文件(为了避免文件保存不全的情况)
//        List<String> alreadySaveNotic = getAlreadySaveNotice(date);
//        log.info("=========已保存公告 :{}",alreadySaveNotic);
//        //处理公告
//        handleNotic(fileMap, alreadySaveNotic);
//        return ReturnT.SUCCESS;
//    }
    @SneakyThrows
    @XxlJob(value = "updateTodayNotice", cron = "0 0/5 * ? * * ", author = "龚敏川", desc = "同步全量的公告")
    public ReturnT<String> updateTodayNotice(String param) {
        Date now = new Date();
        long oldOutTime = DateUtil.between(now, DateUtil.endOfDay(now).toSqlDate(), DateUnit.SECOND);
        String date = DateUtil.format(now, "yyyyMMdd");
        String filePath = "/attachment/";
        String result = filePath.concat(date).concat("/");

        List<? extends HkIisNewsAttachmentBase> attachments = attachmentMapper.selectList(new QueryWrapper<HkIisNewsAttachment>().likeRight(HkIisNewsAttachment.PATH, result).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
        if (CollUtil.isEmpty(attachments)) {
            attachments = attachmentHistoricMapper.selectList(new QueryWrapper<HkIisNewsAttachmentHistoric>().likeRight(HkIisNewsAttachment.PATH, result).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
        }
        log.info("=========当日公告同步 path: {} 表数据量: {}", result,attachments.size());
        if (CollUtil.isEmpty(attachments)) {
            return ReturnT.SUCCESS;
        }
        HashMap<String, String> fileMap = new HashMap<>();
        // 把服务器中 /attachment/20220401/ 路径下的文件 绝对路径.xxx.PDF 和 文件名 返回 fileMap<路径，文件名>
        listFile(result, fileMap);
        log.info("=========当日公告同步 /attachment文件数量 :{}", fileMap.size());
        if (fileMap.isEmpty()) {
            return ReturnT.SUCCESS;
        }

        //通过目录获取目录下已保存mongodb的所有文件(为了避免文件保存不全的情况)
        List<String> alreadySaveNotic = getAlreadySaveNotice(noticeRedisKey.concat(date),date);
        log.info("=========已保存公告 数量 :{}",alreadySaveNotic.size());

        List<? extends List<? extends HkIisNewsAttachmentBase>> groupDatas = groupData(attachments, 10);
        CountDownLatch countDownLatch = new CountDownLatch(groupDatas.size());
        groupDatas.forEach(attachmentList->{
            noticeExecutorService.submit(() -> {
                try {
                    //校验及落库
                    checkAndSave(oldOutTime, attachmentList, fileMap, noticeRedisKey.concat(date), alreadySaveNotic,NoticeJobTypeEnum.save.getJobType());
                } catch (Exception e) {
                    log.error("保存公告 job执行失败", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await();
        log.info("同步公告完成：{}",now);
        return ReturnT.SUCCESS;
    }
    @SneakyThrows
    @XxlJob(value = "repairNoticeData", cron = "0 10 0 * * ? ", author = "龚敏川", desc = "修复公告数据(默认前一天，传参{'startTime':'YYYYMMDD','endTime':'YYYYMMDD'}可修复区间)")
    public ReturnT<String> repairNoticeData(String param) {
        Date startTime=DateUtils.addDays(DateUtils.getFixed(new Date(),0,0,0,0),-1);
        Date endTime=DateUtils.addDays(DateUtils.getFixed(new Date(),23,59,59,999),-1);
        if (StringUtils.isNotBlank(param)) {
            NoticeRepair noticeRepair = JSON.parseObject(param, NoticeRepair.class);
            if (ObjectUtils.isNotEmpty(noticeRepair) && StringUtils.isNotBlank(noticeRepair.getStartTime()) &&  StringUtils.isNotBlank(noticeRepair.getEndTime())) {
                startTime=DateUtils.getFixed(DateUtils.parseDate(noticeRepair.getStartTime()),0,0,0,0);
                endTime=DateUtils.getFixed(DateUtils.parseDate(noticeRepair.getEndTime()),23,59,59,999);
            }
        }

        List<HkIisNewsAttachment> attachments = attachmentMapper.selectList(new QueryWrapper<HkIisNewsAttachment>().between(HkIisNewsAttachment.ENTRY_TIME,startTime,endTime).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
        List<? extends HkIisNewsAttachmentBase> attachmentHistorics = attachmentHistoricMapper.selectList(new QueryWrapper<HkIisNewsAttachmentHistoric>().between(HkIisNewsAttachment.ENTRY_TIME,startTime,endTime).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
        if (CollUtil.isNotEmpty(attachmentHistorics)) {
            List<HkIisNewsAttachment> historics = BeanUtil.copyToList(attachmentHistorics, HkIisNewsAttachment.class);
            attachments.addAll(historics);
        }
        log.info("=========修复公告 startTime: {}  endTime: {}  表数据量: {}", startTime,endTime,attachments.size());
        if (CollUtil.isEmpty(attachments)) {
            return ReturnT.SUCCESS;
        }

        HashMap<String, String> fileMap =getFileData(startTime, endTime);
        log.info("=========修复公告 startTime: {}  endTime: {} /attachment文件数量 :{}", startTime,endTime, fileMap.size());
        if (fileMap.isEmpty()) {
            log.error("修复公告失败，获取不到 startTime: {}  endTime: {}  的公告文件", startTime,endTime);
            return ReturnT.FAIL;
        }

        //通过目录获取目录下已保存mongodb的所有文件(为了避免文件保存不全的情况)
        List<String> alreadySaveNotic = getRepairAlreadySaveNotice(startTime, endTime);
        log.info("=========已保存公告 数量 :{}",alreadySaveNotic.size());

        List<? extends List<? extends HkIisNewsAttachmentBase>> groupDatas = groupData(attachments, 20);
        CountDownLatch countDownLatch = new CountDownLatch(groupDatas.size());
        groupDatas.forEach(attachmentList->{
            noticeExecutorService.submit(() -> {
                try {
                    //校验及落库
                    checkAndSave(null, attachmentList, fileMap, null, alreadySaveNotic, NoticeJobTypeEnum.repair.getJobType());
                } catch (Exception e) {
                    log.error("修复公告 job执行失败", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await();
        log.info("修复公告完成 startTime: {}  endTime: {}", startTime,endTime);
        return ReturnT.SUCCESS;
    }

    //获取修复公告时的已保存文件信息
    private List<String> getRepairAlreadySaveNotice(Date stateTime, Date endTime) {
        List<String> dirsList=new ArrayList<>();
        while (stateTime.compareTo(endTime)<=0){
            String date = DateUtil.format(stateTime, "yyyyMMdd");
            dirsList.add(date);
            stateTime=DateUtils.addDays(stateTime,1);
        }
        Query query = Query.query(Criteria.where("dirs").in(dirsList));
        List<StockUtsNoticeEntityV2> noticeEntitys = mongoTemplate.find(query, StockUtsNoticeEntityV2.class);
        List<String> alreadySaveNotic =Lists.newArrayList();
        if (CollUtil.isNotEmpty(noticeEntitys)) {
            Map<String, Integer> alreadySaveNoticMap = noticeEntitys.stream().collect(Collectors.toMap(notice -> String.format(noticeItem,notice.getFileName(),notice.getLineId(),notice.getCategoryId(),notice.getStockCode()), notice -> 1, (k1, k2) -> k1));
            alreadySaveNotic=Lists.newArrayList(alreadySaveNoticMap.keySet());
        }
        return alreadySaveNotic;
    }

    private  HashMap<String, String> getFileData(Date stateTime, Date endTime) {
        HashMap<String, String> fileMap = new HashMap<>();
        // 把服务器中 /attachment/20220401/ 路径下的文件 绝对路径.xxx.PDF 和 文件名 返回 fileMap<路径，文件名>
        while (stateTime.compareTo(endTime)<=0){
            String date = DateUtil.format(stateTime, "yyyyMMdd");
            String filePath = "/attachment/";
            String result = filePath.concat(date).concat("/");
            listFile(result, fileMap);
            stateTime=DateUtils.addDays(stateTime,1);
        }

        return fileMap;
    }

    //校验及落库
    private void checkAndSave(Long oldOutTime, List<? extends HkIisNewsAttachmentBase> attachments, HashMap<String, String> fileMap, String noticeRedisKey, List<String> alreadySaveNotic,Integer jobType) {
        List<String> lineIds = attachments.stream().map(attachment -> attachment.getLineId()).collect(Collectors.toList());
        Map<String, ? extends List<? extends HkIisNewsHeadlineBase>> headMap = getNewsHeadMap(lineIds);
        Map<String, ? extends List<? extends HkIisNewsCateRefBase>> newsCateMap = getNewsCateMap(lineIds);
        Map<String, ? extends List<? extends HkIisNewsSecurityRefBase>> newsSecurityMap = getNewSecurityMap(lineIds);
        List<StockUtsNoticeEntityV2> noticeALL =new ArrayList<>();
        attachments.forEach(attachment->{
            String fileName = fileMap.get(attachment.getPath());
            String time = attachment.getPath().split("/")[2];
            List<? extends HkIisNewsHeadlineBase> heads = headMap.get(attachment.getLineId());
            List<? extends HkIisNewsCateRefBase> cates = newsCateMap.get(attachment.getLineId());
            List<? extends HkIisNewsSecurityRefBase> securitys = newsSecurityMap.get(attachment.getLineId());
            if (checkDate(attachment, fileName, heads, cates, securitys,jobType)){
                //保存公告信息
                List<StockUtsNoticeEntityV2> notices = saveNoticeToMongoDB( alreadySaveNotic, attachment, fileName, time, heads, cates, securitys);
                //更新成功--保存到minio文件系统
                if (CollUtil.isNotEmpty(notices)) {
                    noticeALL.addAll(notices);
                    Boolean fileIsSave = saveToMinio(attachment, fileName, time);
                    if (fileIsSave){
                        mongoTemplate.insertAll(notices);
                        if (NoticeJobTypeEnum.save.getJobType()==jobType) {
                            notices.forEach(notice -> redisClient.hset(noticeRedisKey,String.format(noticeItem,notice.getFileName(),notice.getLineId(),notice.getCategoryId(),notice.getStockCode()),1, oldOutTime));
                        }
                    }
                }
            }

        });
        log.info("线程：{} 公告落库完成：{}",Thread.currentThread().getName(),noticeALL.size());
    }
    //        更新成功--保存到minio文件系统
    private Boolean saveToMinio(HkIisNewsAttachmentBase attachment, String fileName, String time) {
            Boolean fileIsSave =false;
            try {
                InputStream in = new FileInputStream(attachment.getPath());
                fileIsSave = fileStorageManager.putObject(time.concat("/").concat(fileName), "notice-attach", in);
                in.close();
                if (!fileIsSave){
                    log.error("公告文件上传失败，filePath:{} fileIsSave:{}" ,attachment.getPath(),fileIsSave);
                }
            } catch (Exception e) {
                log.error("公告文件上传失败，filePath:{}" ,attachment.getPath(),e);
            }
            return fileIsSave;
    }

    //校验数据
    private boolean checkDate(HkIisNewsAttachmentBase attachment,
                              String fileName,
                              List<? extends HkIisNewsHeadlineBase> heads,
                              List<? extends HkIisNewsCateRefBase> cates,
                              List<? extends HkIisNewsSecurityRefBase> securitys,
                              Integer jobType) {
        if (StringUtils.isBlank(fileName)){
            if (NoticeJobTypeEnum.repair.getJobType()==jobType) {
                log.error("公告文件 ：{} 同步失败，/attachment 没有该文件", attachment.getPath());
            }
            return false;
        }
        if (CollUtil.isEmpty(heads)) {
            if (NoticeJobTypeEnum.repair.getJobType()==jobType) {
                log.error("公告文件 ：{} 同步失败，查不到标题信息", attachment.getPath());
            }
            return false;
        }
        if (CollUtil.isEmpty(cates)){
            if (NoticeJobTypeEnum.repair.getJobType()==jobType) {
                log.error("公告文件 ：{} 同步失败，查不到公告类别关联信息", attachment.getPath());
            }
            return false;
        }
        if (CollUtil.isEmpty(securitys)){
            return false;
        }
        return true;
    }

    //保存公告信息
    private List<StockUtsNoticeEntityV2> saveNoticeToMongoDB(List<String> alreadySaveNotic,
                                     HkIisNewsAttachmentBase attachment,
                                     String fileName,
                                     String time,
                                     List<? extends HkIisNewsHeadlineBase> heads,
                                     List<? extends HkIisNewsCateRefBase> cates,
                                     List<? extends HkIisNewsSecurityRefBase> securitys) {
        List<StockUtsNoticeEntityV2> notices=Lists.newArrayList();
        heads.forEach(head->{
            cates.forEach(cate->{
                securitys.forEach(security->{
                    String noticSign = String.format(noticeItem, fileName, attachment.getLineId(), cate.getCategoryId(), security.getSecCode());
                    //校验公告信息是否保存mongodb
                    if (!alreadySaveNotic.contains(noticSign)) {

                        Query query = Query.query(Criteria.where("lineId").is(attachment.getLineId()).and("rawPath").is(attachment.getPath()).and("stockCode").is(security.getSecCode()).and("categoryId").is(cate.getCategoryId()));
                        boolean pathExists = mongoTemplate.exists(query, StockUtsNoticeEntityV2.class);
                        if (!pathExists) {
                            StockUtsNoticeEntityV2 stockUtsNoticeEntity = StockUtsNoticeEntityV2.builder()
                                    .categoryId(cate.getCategoryId())
                                    .dateLine(head.getDateLine())
                                    .lineId(attachment.getLineId())
                                    .fileName(fileName)
                                    .headLine(head.getHeadline())
                                    .rawPath(attachment.getPath())
                                    .fileDesc(attachment.getDescription())
                                    .attachmentNum(head.getAttachmentNum())
                                    .language(head.getLanguageId())
                                    .dirs(time)
                                    .stockCode(security.getSecCode())
                                    .build();
                            notices.add(stockUtsNoticeEntity);
                        }

                    }
                });
            });
        });
        return notices;
    }

    //获取公告股票关联
    private Map<String, ? extends List<? extends HkIisNewsSecurityRefBase>> getNewSecurityMap(List<String> lineIds) {
        List<HkIisNewsSecurityRefHistoric> newsSecurityRefs = securityRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsSecurityRefHistoric>().in(HkIisNewsSecurityRefBase.LINE_ID, lineIds));
        List<? extends HkIisNewsSecurityRefBase> newsSecurityRefHistorics = securityRefMapper.selectList(new QueryWrapper<HkIisNewsSecurityRef>().in(HkIisNewsSecurityRefBase.LINE_ID, lineIds));
        if (CollUtil.isNotEmpty(newsSecurityRefHistorics)) {
            List<HkIisNewsSecurityRefHistoric> historics = BeanUtil.copyToList(newsSecurityRefHistorics, HkIisNewsSecurityRefHistoric.class);
            newsSecurityRefs.addAll(historics);
        }
        Map<String, ? extends List<? extends HkIisNewsSecurityRefBase>> newsSecurityMap = newsSecurityRefs.stream().collect(Collectors.groupingBy(security -> security.getLineId()));
        return newsSecurityMap;
    }
    //获取公告别类关联
    private Map<String, ? extends List<? extends HkIisNewsCateRefBase>> getNewsCateMap(List<String> lineIds) {
        List<HkIisNewsCateRef> newsCateRefs = cateRefMapper.selectList(new QueryWrapper<HkIisNewsCateRef>().in(HkIisNewsCateRefBase.LINE_ID, lineIds));
        List<? extends HkIisNewsCateRefBase> newsCateRefHistorics = cateRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsCateRefHistoric>().in(HkIisNewsCateRefBase.LINE_ID, lineIds));
        if (CollUtil.isNotEmpty(newsCateRefHistorics)) {
            List<HkIisNewsCateRef> historics = BeanUtil.copyToList(newsCateRefHistorics, HkIisNewsCateRef.class);
            newsCateRefs.addAll(historics);
        }
        Map<String, ? extends List<? extends HkIisNewsCateRefBase>> newsCateMap = newsCateRefs.stream().collect(Collectors.groupingBy(cate -> cate.getLineId()));
        return newsCateMap;
    }
    //获取公告标题
    private Map<String, ? extends List<? extends HkIisNewsHeadlineBase>> getNewsHeadMap(List<String> lineIds) {
        List<HkIisNewsHeadline> newsHeadlines = headlineMapper.selectList(new QueryWrapper<HkIisNewsHeadline>().in(HkIisNewsHeadline.LINE_ID, lineIds));
        List<? extends HkIisNewsHeadlineBase>  newsHeadlineHistorics = headlineHistoricMapper.selectList(new QueryWrapper<HkIisNewsHeadlineHistoric>().in(HkIisNewsHeadline.LINE_ID, lineIds));
        if (CollUtil.isNotEmpty(newsHeadlineHistorics)) {
            List<HkIisNewsHeadline> historics = BeanUtil.copyToList(newsHeadlineHistorics, HkIisNewsHeadline.class);
            newsHeadlines.addAll(historics);
        }
        Map<String, ? extends List<? extends HkIisNewsHeadlineBase>> headMap = newsHeadlines.stream().collect(Collectors.groupingBy(head -> head.getLineId()));
        return headMap;
    }

    //分组
    private <T> List<List<T>> groupData(List<T> list, Integer group) {

        List<List<T>> groups = Lists.newArrayList();
        Integer size = list.size();
        if (size<group){
            group=size;
        }
        //倍数因子
        Integer factor = size / group;
        //余数
        Integer remainder = size % group;
        //起始坐标
        Integer beginIndex = 0;
        //结束坐标
        Integer endIndex = 0;
        for (Integer i = 1; i <= group; i++) {
            endIndex = beginIndex + factor;
            if (remainder >= i) {
                endIndex++;
            }
            List<T> datas = list.subList(beginIndex, endIndex);
            beginIndex = endIndex;
            groups.add(datas);
        }
        return groups;
    }
    //通过目录获取目录下已保存mongodb的所有文件(为了避免文件保存不全的情况)
    private List<String> getAlreadySaveNotice(String noticeRedisKey,String date) {

        //map<xxx.PDF_lineId_categoryId_secCode,1>
        Map<String,Integer> pathMap = redisClient.hmget(noticeRedisKey);
        List<String> alreadySaveNotic= Lists.newArrayList();
        if (MapUtil.isNotEmpty(pathMap)) {
            alreadySaveNotic=Lists.newArrayList(pathMap.keySet());
        }else {
            //缓存没有则查数据库
            List<StockUtsNoticeEntityV2> noticeEntitys = stockUtsNoticeV2Dao.getNoticByDirs(date);
            if (CollUtil.isNotEmpty(noticeEntitys)) {
                Map<String, Integer> alreadySaveNoticMap = noticeEntitys.stream().collect(Collectors.toMap(notice -> String.format(noticeItem,notice.getFileName(),notice.getLineId(),notice.getCategoryId(),notice.getStockCode()), notice -> 1, (k1, k2) -> k1));
                alreadySaveNotic=Lists.newArrayList(alreadySaveNoticMap.keySet());
                redisClient.hmset(noticeRedisKey,alreadySaveNoticMap);
            }
        }
        return alreadySaveNotic;
    }

//    //处理公告
//    private void handleNotic(HashMap<String, String> fileMap, List<String> alreadySaveNotic) throws InterruptedException {
//        //对map进行切分，100个为map为1个list元素
//        List<Map<String, String>> mapList = CollectUtils.splitMap(fileMap, 100);
//        CountDownLatch countDownLatch = new CountDownLatch(mapList.size());
//        for (final Map<String, String> stringMap : mapList) {
//            noticeExecutorService.submit(() -> {
//                try {
//                    doDataSync(stringMap, alreadySaveNotic);
//                } catch (Exception e) {
//                    log.error("job执行失败", e);
//                    XxlJobLogger.log("执行失败={}", e);
//                } finally {
//                    countDownLatch.countDown();
//                }
//            });
//            log.info("保存成功");
//        }
//        countDownLatch.await();
//    }

    /**
     * 缓存股票市场数据
     *
     * @param
     * @return
     */
    /*@SneakyThrows
    @XxlJob(value = "updateNoticeV2", author = "陈玉", desc = "同步全量的公告")
    public ReturnT<String> updateAllNotice(String param) {
        //读取文件
        String filePath = "/attachment/";
        HashMap<String, String> fileMap = new HashMap<>();
        listFile(filePath, fileMap);
        if (fileMap.isEmpty()) {
            return ReturnT.SUCCESS;
        }
        List<Map<String, String>> mapList = CollectUtils.splitMap(fileMap, 50000);
        CountDownLatch countDownLatch = new CountDownLatch(mapList.size());
        for (final Map<String, String> stringMap : mapList) {
            noticeExecutorService.submit(() -> {
                try {
                    doDataSync(stringMap);
                } catch (Exception e) {
                    log.error("job执行失败", e);
                    XxlJobLogger.log("执行失败={}", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        return ReturnT.SUCCESS;
    }*/

//    private void doDataSync(Map<String, String> fileMap,List<String> alreadySaveNotic) {
//        fileMap.forEach((key, value) -> {
//            String redisKey = "notice:".concat(key);
//            boolean flag = redisClient.hasKey(redisKey);
//            log.info("=========公告文件 key :{} 是否已同步 :{}",key,flag);
//
//            //key不存在 或者 公告数据未保存到mongodb 时则处理
//            if (!flag || (CollUtil.isNotEmpty(alreadySaveNotic) && !alreadySaveNotic.contains(key))) {
//                //key不存在
//                String splitPath = null;
//                try {
//                    //splitPath 拿到的是日期
//                    splitPath = key.split("/")[2];
//                } catch (Exception e) {
//                    log.error("路径参数有问题", e);
//                    XxlJobLogger.log("路径参数有问题", e);
//                }
//                //获取新闻附件/公告
//                HkIisNewsAttachmentBase attachment = getAttachment(key.replace("\\\\", "/"));
//                if (attachment != null) {
//                    List<Integer> flags = new ArrayList<>();
//                    String lineId = attachment.getLineId();
//                    HkIisNewsHeadlineBase headLine = getHeadLine(lineId);
//                    if (headLine != null) {
//                        List<? extends HkIisNewsCateRefBase> cateRefs = getCategoryIds(lineId);
//                        List<? extends HkIisNewsSecurityRefBase> codes = getStockCode(lineId);
//                        String finalSplitPath = splitPath;
//                        cateRefs.forEach(item -> {
//                            StockUtsNoticeEntityV2 stockUtsNoticeEntity = StockUtsNoticeEntityV2.builder()
//                                    .categoryId(item.getCategoryId())
//                                    .dateLine(headLine.getDateLine())
//                                    .lineId(lineId)
//                                    .fileName(value)
//                                    .headLine(headLine.getHeadline())
//                                    .rawPath(key)
//                                    .fileDesc(attachment.getDescription())
//                                    .attachmentNum(headLine.getAttachmentNum())
//                                    .language(headLine.getLanguageId())
//                                    .build();
//                            if (null != finalSplitPath) {
//                                stockUtsNoticeEntity.setDirs(finalSplitPath);
//                            }
//
//                            codes.forEach(it -> {
//                                stockUtsNoticeEntity.setStockCode(it.getSecCode());
//                                Integer update = stockUtsNoticeV2Dao.saveOrUpdate(stockUtsNoticeEntity,flags);
//                                if (update == 1) {
//                                    //更新成功--保存到minio文件系统
//                                        try {
//                                            InputStream in = new FileInputStream(key);
//                                            fileStorageManager.putObject(finalSplitPath == null ? value : finalSplitPath.concat("/").concat(value), "notice-attach", in);
//                                            in.close();
//                                        } catch (FileNotFoundException e2) {
//                                            log.error("文件不存在!!!!文件名称{},异常信息{}", key, e2);
//                                        } catch (IOException e1) {
//                                            log.error("stream close error !!", e1);
//                                        } catch (Exception e) {
//                                            log.info("文件保存失败:" + key);
//                                            log.error(e.getMessage(), e);
//                                        }
//
//                                }
//                            });
//                        });
//                    }
//
//                    if (this.calFlags(flags)){
//                        redisClient.set(redisKey, "1");
//                    }
//                }
//            }
//        });
//
//    }

//    private boolean calFlags(List<Integer> flags) {
//        int flag = 1;
//        for (Integer i : flags) {
//            flag = i & flag;
//        }
//        return flag == 1;
//    }

//    private List<? extends HkIisNewsSecurityRefBase> getStockCode(String lineId) {
//
//        List<? extends HkIisNewsSecurityRefBase> newsSecurityRefs = securityRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsSecurityRefHistoric>()
//                .eq(HkIisNewsSecurityRefBase.LINE_ID, lineId));
//        if (newsSecurityRefs.isEmpty()) {
//            newsSecurityRefs = securityRefMapper.selectList(new QueryWrapper<HkIisNewsSecurityRef>()
//                    .eq(HkIisNewsSecurityRefBase.LINE_ID, lineId));
//        }
//        return newsSecurityRefs;
//    }

//    private List<? extends HkIisNewsCateRefBase> getCategoryIds(String lineId) {
//        List<? extends HkIisNewsCateRefBase> cateRefs = cateRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsCateRefHistoric>()
//                .eq(HkIisNewsCateRefBase.LINE_ID, lineId));
//        if (cateRefs == null || cateRefs.isEmpty()) {
//            cateRefs = cateRefMapper.selectList(new QueryWrapper<HkIisNewsCateRef>()
//                    .eq(HkIisNewsCateRefBase.LINE_ID, lineId));
//        }
//        return cateRefs;
//    }

//    private HkIisNewsHeadlineBase getHeadLine(String lineId) {
//        HkIisNewsHeadlineBase hkIisNewsHeadlineBase = headlineHistoricMapper.selectOne(new QueryWrapper<HkIisNewsHeadlineHistoric>()
//                .eq(HkIisNewsHeadline.LINE_ID, lineId));
//        if (hkIisNewsHeadlineBase == null) {
//            hkIisNewsHeadlineBase = headlineMapper.selectOne(new QueryWrapper<HkIisNewsHeadline>()
//                    .eq(HkIisNewsHeadline.LINE_ID, lineId));
//        }
//        return hkIisNewsHeadlineBase;
//    }

//    private HkIisNewsAttachmentBase getAttachment(String key) {
//        try {
//            //先查历史表
//            HkIisNewsAttachmentBase attachment = attachmentHistoricMapper.selectOne(new QueryWrapper<HkIisNewsAttachmentHistoric>()
//                    .eq(HkIisNewsAttachment.PATH, key).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
//            if (attachment == null) {
//                attachment = attachmentMapper.selectOne(new QueryWrapper<HkIisNewsAttachment>()
//                        .eq(HkIisNewsAttachment.PATH, key).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
//            }
//            return attachment;
//        } catch (Exception e) {
//            log.error("找不到附件信息:" + key);
//            XxlJobLogger.log("找不到附件信息" + key);
//            return null;
//        }
//    }

    //  把/attachment/20220401/ 路径下的文件绝对路径和文件名返回
    public void listFile(String path, Map<String, String> fileMap) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        listFile(file2.getAbsolutePath(), fileMap);
                    } else {
                        fileMap.put(file2.getAbsolutePath(), file2.getName());
                    }
                }
            }
        } else {
            log.info("文件目录不存在,path{},时间{}", path, System.currentTimeMillis());
        }
    }


}
