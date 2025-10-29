package com.vv.finance.investment.bg.handler.uts.f10;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constant.RepairNotice;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.StockUtsNoticeV2Dao;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntityV2;
import com.vv.minio.starter.core.FileStorageManager;
import com.vv.minio.starter.core.MinioTemplate;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author luoyj
 * @date 2022/4/19
 * @description
 */
@Slf4j
@Component
public class NoticeHandler {

    @Autowired
    private HkIisNewsAttachmentHistoricMapper attachmentHistoricMapper;
    @Autowired
    private HkIisNewsAttachmentMapper attachmentMapper;
    @Autowired
    private HkIisNewsHeadlineHistoricMapper headlineHistoricMapper;
    @Autowired
    private HkIisNewsHeadlineMapper headlineMapper;
    @Autowired
    private HkIisNewsSecurityRefHistoricMapper securityRefHistoricMapper;
    @Autowired
    private HkIisNewsSecurityRefMapper securityRefMapper;
    @Autowired
    private HkIisNewsCateRefHistoricMapper cateRefHistoricMapper;
    @Autowired
    private HkIisNewsCateRefMapper cateRefMapper;
    @Autowired
    private StockUtsNoticeV2Dao stockUtsNoticeV2Dao;
//    @Autowired
//    private MinioTemplate minioTemplate;
    @Autowired
    private FileStorageManager fileStorageManager;
    @Autowired
    private RedisClient redisClient;


    public ResultT doRepair(String lineId,HkIisNewsAttachmentBase attachment2){

        //查询新闻附件
        HkIisNewsAttachmentBase attachment;
        if (attachment2 != null){
            attachment = attachment2;
        }else {
            attachment = this.getAttachmentByLineId(lineId);
        }

        if (Objects.isNull(attachment)) {
            log.info("NoticeHandler.doRepair(),attachment is null, lineId:{}",lineId);
            return ResultT.fail("attachment is null, lineId:"+lineId);
        }
        //标识
        String repairKey = RepairNotice.REPAIR_NOTICE_KEY.concat(attachment.getPath());
        //查询新闻标题信息主档
        HkIisNewsHeadlineBase headLine = this.getHeadLine(lineId);

        String redisKey = "notice:".concat(attachment.getPath());
        List<Integer> flags = new ArrayList<>();
        if (headLine != null) {
            //查询新闻类别关联档
            List<? extends HkIisNewsCateRefBase> cateRefs = getCategoryIds(lineId);
            //查询新闻股票关联表
            List<? extends HkIisNewsSecurityRefBase> codes = getStockCode(lineId);

            String finalSplitPath = this.splitPathToDirs(attachment.getPath(),2);
            String fileName = this.splitPathToDirs(attachment.getPath(),3);

            cateRefs.forEach(item -> {
                StockUtsNoticeEntityV2 stockUtsNoticeEntity = StockUtsNoticeEntityV2.builder()
                        .categoryId(item.getCategoryId())
                        .dateLine(headLine.getDateLine())
                        .lineId(lineId)
                        .fileName(fileName)
                        .headLine(headLine.getHeadline())
                        .rawPath(attachment.getPath())
                        .fileDesc(attachment.getDescription())
                        .attachmentNum(headLine.getAttachmentNum())
                        .language(headLine.getLanguageId())
                        .build();
                if (null != finalSplitPath) {
                    stockUtsNoticeEntity.setDirs(finalSplitPath);
                }
                codes.forEach(it -> {
                    stockUtsNoticeEntity.setStockCode(it.getSecCode());
                    Integer update = stockUtsNoticeV2Dao.saveOrUpdate(stockUtsNoticeEntity,flags);
                    if (update == 1) {
                        try {
                            InputStream in = new FileInputStream(attachment.getPath());
                            fileStorageManager.putObject(finalSplitPath == null ? fileName : finalSplitPath.concat("/").concat(fileName), "notice-attach", in);
                            in.close();
                        } catch (FileNotFoundException e2) {
                            log.error("文件不存在!!!!文件名称{},异常信息{}", attachment.getPath(), e2);
                        } catch (IOException e1) {
                            log.error("stream close error !!", e1);
                        } catch (Exception e) {
                            log.error("文件保存失败:" + attachment.getPath());
                            log.error(e.getMessage(), e);
                        }

                    }
                });
            });
        }

        if (this.calFlags(flags)){
            redisClient.set(repairKey,"1",30, TimeUnit.DAYS);
            redisClient.set(redisKey, "1");
            log.info("【修补公告数据成功】 lineId:{}",lineId);
        }

        return ResultT.success();
    }

    /**
     * 根据path，查询新闻附件信息
     * @param path
     * @return
     */
    public HkIisNewsAttachmentBase getAttachmentByPath(String path) {
        try {
            HkIisNewsAttachmentBase attachment = attachmentHistoricMapper.selectOne(new QueryWrapper<HkIisNewsAttachmentHistoric>()
                    .eq(HkIisNewsAttachment.PATH, path).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
            if (attachment == null) {
                attachment = attachmentMapper.selectOne(new QueryWrapper<HkIisNewsAttachment>()
                        .eq(HkIisNewsAttachment.PATH, path).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
            }
            return attachment;
        } catch (Exception e) {
            log.info("##### NoticeHandler ##### 查找附件异常:" + path,e);
            return null;
        }
    }

    /**
     * 根据绝对路径截取 文件夹：index=2 /文件名：index=3
     * @param path
     * @return
     */
    private String splitPathToDirs(String path,int index){
        return path.split("/")[index];
    }

    /**
     * 根据lineId，查询新闻附件信息
     * @param lineId
     * @return
     */
    private HkIisNewsAttachmentBase getAttachmentByLineId(String lineId) {
        try {
            HkIisNewsAttachmentBase attachment = attachmentHistoricMapper.selectOne(new QueryWrapper<HkIisNewsAttachmentHistoric>()
                    .eq(HkIisNewsAttachment.LINE_ID, lineId).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
            if (attachment == null) {
                attachment = attachmentMapper.selectOne(new QueryWrapper<HkIisNewsAttachment>()
                        .eq(HkIisNewsAttachment.LINE_ID, lineId).eq(HkIisNewsAttachment.DOWNLOAD_FLAG, "Y"));
            }
            return attachment;
        } catch (Exception e) {
            log.error("NoticeHandler.getAttachmentByLineId() 找不到附件信息:" + lineId);
            return null;
        }
    }

    /**
     * 根据lineId，查询新闻公告标题
     * @param lineId
     * @return
     */
    private HkIisNewsHeadlineBase getHeadLine(String lineId) {
        HkIisNewsHeadlineBase hkIisNewsHeadlineBase = headlineHistoricMapper.selectOne(new QueryWrapper<HkIisNewsHeadlineHistoric>()
                .eq(HkIisNewsHeadline.LINE_ID, lineId));
        if (hkIisNewsHeadlineBase == null) {
            hkIisNewsHeadlineBase = headlineMapper.selectOne(new QueryWrapper<HkIisNewsHeadline>()
                    .eq(HkIisNewsHeadline.LINE_ID, lineId));
        }
        return hkIisNewsHeadlineBase;
    }

    /**
     * 查询新闻类别关联档
     * @param lineId
     * @return
     */
    private List<? extends HkIisNewsCateRefBase> getCategoryIds(String lineId) {
        List<? extends HkIisNewsCateRefBase> cateRefs = cateRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsCateRefHistoric>()
                .eq(HkIisNewsCateRefBase.LINE_ID, lineId));
        if (cateRefs == null || cateRefs.isEmpty()) {
            cateRefs = cateRefMapper.selectList(new QueryWrapper<HkIisNewsCateRef>()
                    .eq(HkIisNewsCateRefBase.LINE_ID, lineId));
        }
        return cateRefs;
    }

    /**
     * 查询新闻股票关联表
     * @param lineId
     * @return
     */
    private List<? extends HkIisNewsSecurityRefBase> getStockCode(String lineId) {

        List<? extends HkIisNewsSecurityRefBase> newsSecurityRefs = securityRefHistoricMapper.selectList(new QueryWrapper<HkIisNewsSecurityRefHistoric>()
                .eq(HkIisNewsSecurityRefBase.LINE_ID, lineId));
        if (newsSecurityRefs.isEmpty()) {
            newsSecurityRefs = securityRefMapper.selectList(new QueryWrapper<HkIisNewsSecurityRef>()
                    .eq(HkIisNewsSecurityRefBase.LINE_ID, lineId));
        }
        return newsSecurityRefs;
    }

    /**
     * 计算是否所有都成功保存到mongodb
     * @param flags
     * @return
     */
    private boolean calFlags(List<Integer> flags) {
        int flag = 1;
        for (Integer i : flags) {
            flag = i & flag;
        }
        return flag == 1;
    }
}
