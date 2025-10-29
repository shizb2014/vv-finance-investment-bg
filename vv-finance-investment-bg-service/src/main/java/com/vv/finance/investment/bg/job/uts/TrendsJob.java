package com.vv.finance.investment.bg.job.uts;

import com.vv.finance.investment.bg.handler.trends.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @Auther: shizb
 * @Date: 2023/12/25 - 12 - 25 - 14:43
 * @Description: com.vv.finance.investment.bg.job.uts
 * @version: 1.0
 */
@Component
@Slf4j
public class TrendsJob {

    @Resource
    XNHK0127Handler xnhk0127Handler;

    @Resource
    XNHK0201Handler xnhk0201Handler;

    @Resource
    XNHK0204Handler xnhk0204Handler;

    @Resource
    XNHK0207Handler xnhk0207Handler;
    @Resource
    XNHK0311Handler xnhk0311Handler;
    @Resource
    XNHK0318Handler xnhk0318Handler;
    @Resource
    XNHKS0308Handler xnhks0308Handler;
    @Resource
    XNHKS0310Handler xnhks0310Handler;
    @Resource
    XNHKS0314Handler xnhks0314Handler;
    @Resource
    XNHKS0317Handler xnhks0317Handler;

    @XxlJob(value = "updateCompanyTrends", cron = "0 0 3 ? * 2,3,4,5,6 *", author = "史志彪", desc = "更新公司动向数据")
    public ReturnT<String> updateCompanyTrends(String param) {
        long t = System.currentTimeMillis();
        Integer minusDays = 30;
        if(StringUtils.isNotEmpty(param)){
           minusDays = Integer.valueOf(param);
        }
        log.info("更新公司动向数据:{}",minusDays);
        xnhk0127Handler.syncRecent(minusDays);
        xnhk0201Handler.syncRecent(minusDays);
        xnhk0204Handler.syncRecent(minusDays);
        xnhk0207Handler.syncRecent(minusDays);
        xnhk0311Handler.syncRecent(minusDays);
        xnhks0308Handler.syncRecent(minusDays);
        xnhks0310Handler.syncRecent(minusDays);
        xnhks0314Handler.syncRecent(minusDays);
        xnhks0317Handler.syncRecent(minusDays);
        xnhk0318Handler.syncRecent(minusDays);
        log.info("更新公司动向数据耗时：{}", System.currentTimeMillis() - t);

        return ReturnT.SUCCESS;
    }
}
