package com.vv.finance.investment.bg.job.glue;

import com.vv.finance.investment.bg.handler.information.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;

import javax.annotation.Resource;

/**
 * @author hamilton
 * @date 2021/11/10 16:37
 */
public class StockNewsHandler extends IJobHandler {

    @Resource
    private News24HoursHandler news24HoursHandler;
    @Resource
    NewsCalendarHandler newNewsCalendarHandler;
    @Resource
    NewsHkHandler newNewsHkHandler;
    @Resource
    NewsHkHqHandler newsHkHqHandler;

    @Resource
    NewsUsHandler newNewsUsHandler;

    //TODO-luoyj 需要排查是否在xxl-job界面配置了glue模式（直接贴代码执行任务）
    //应该不会执行了，使用canal后遗留代码; 而且 Xxl-job源码已经修改过，没有@JobHandler注解
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        news24HoursHandler.sync();;
        newNewsCalendarHandler.sync();
        newNewsHkHandler.sync();
        newsHkHqHandler.sync();
        newNewsUsHandler.sync();
        return ReturnT.SUCCESS;
    }


}
