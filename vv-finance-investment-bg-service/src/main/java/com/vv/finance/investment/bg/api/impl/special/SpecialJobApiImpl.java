//package com.vv.finance.investment.bg.api.impl.special;
//
//import com.vv.finance.investment.bg.api.special.SpecialJobApi;
//import com.vv.finance.investment.bg.job.special.SpecialJob;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.config.annotation.DubboService;
//
//import javax.annotation.Resource;
//
///**
// * description: SpecialJobApiImpl
// * date: 2024/8/1 15:54
// * author: yeyexin
// */
//@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
//@Slf4j
//@RequiredArgsConstructor
//public class SpecialJobApiImpl implements SpecialJobApi {
//
//    @Resource
//    SpecialJob specialJob;
//
//    @Override
//    public void dealParallelCodeCurrentDay(String param) {
//        specialJob.dealParallelCodeCurrentDay(param);
//    }
//
//    @Override
//    public void dealParallelCodeNextDay(String param){
//        specialJob.dealParallelCodeNextDay(param);
//    }
//
//    @Override
//    public void dealReuseCode(String param){
//        specialJob.dealReuseCode(param);
//    }
//
//    @Override
//    public void dealChangeCode(String param){
//        specialJob.dealChangeCode(param);
//    }
//}
