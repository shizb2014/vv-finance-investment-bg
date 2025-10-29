package com.vv.finance.investment.bg.job.uts;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constant.RepairNotice;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsAttachmentBase;
import com.vv.finance.investment.bg.handler.uts.f10.NoticeHandler;
import com.vv.finance.investment.bg.utils.CollectUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author luoyj
 * @date 2022/4/20
 * @description
 */
@Slf4j
@Component
public class RepairNoticeJob {
    private final static String filePath = "/attachment/";

    @Autowired
    private NoticeHandler noticeHandler;
    @Autowired
    private RedisClient redisClient;

    ExecutorService repairNoticeExecutorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(4, 8, 0,TimeUnit.SECONDS, new LinkedBlockingQueue<>(30000), new CustomizableThreadFactory("repair-notice")));

//    @SneakyThrows
//    @XxlJob(value = "repairNotice", cron = "0 0 3 L * ? ", author = "罗永佳", desc = "修补公告数据(已弃用)")
//    public ReturnT<String> repairNotice(String param) {
//        log.info("##### RepairNoticeJob ##### start doRepairNoticeJob ...");
//        //key:fileAbsolutePath value:fileName
//        HashMap<String, String> fileMap = new HashMap<>();
//        this.listFile(filePath, fileMap);
//        if (fileMap.isEmpty()) {
//            log.info("##### RepairNoticeJob ##### fileMap.isEmpty !!!");
//            return ReturnT.SUCCESS;
//        }
//
//        log.info("##### RepairNoticeJob ##### fileMap.size:{}",fileMap.size());
//        List<Map<String, String>> mapList = CollectUtils.splitMap(fileMap, 1000);
//        CountDownLatch countDownLatch = new CountDownLatch(mapList.size());
//
//        for (final Map<String, String> stringMap : mapList) {
//            repairNoticeExecutorService.submit(() -> {
//                try {
//                    this.doDataSync(stringMap);
//                } catch (Exception e) {
//                    log.error("##### RepairNoticeJob #####",e);
//                    XxlJobLogger.log("执行失败={}", e);
//                } finally {
//                    countDownLatch.countDown();
//                }
//            });
//
//        }
//        countDownLatch.await();
//        log.info("##### RepairNoticeJob ##### end doRepairNoticeJob ...");
//
//        return ReturnT.SUCCESS;
//    }

    private void doDataSync(Map<String, String> fileMap) {
        fileMap.forEach((k,v) -> {
            //标识
            String repairKey = RepairNotice.REPAIR_NOTICE_KEY.concat(k);
            boolean flag = redisClient.hasKey(repairKey);
            if (!flag){
                HkIisNewsAttachmentBase hkIisNewsAttachmentBase = noticeHandler.getAttachmentByPath(k);
                if (hkIisNewsAttachmentBase != null) {
                    String lineId = hkIisNewsAttachmentBase.getLineId();
                    noticeHandler.doRepair(lineId, hkIisNewsAttachmentBase);
                }
            }else {
                redisClient.expire(repairKey,30,TimeUnit.DAYS);
            }
        });

    }


    private void listFile(String path, Map<String, String> fileMap) {
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
