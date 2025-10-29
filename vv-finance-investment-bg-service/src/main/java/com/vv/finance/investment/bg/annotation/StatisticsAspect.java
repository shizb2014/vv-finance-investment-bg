package com.vv.finance.investment.bg.annotation;

import cn.hutool.core.util.ReflectUtil;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName StatisticsAspect
 * @Deacription 统计切面
 * @Author lh.sz
 * @Date 2021年09月18日 16:15
 **/
@Aspect
@Component
@Slf4j
public class StatisticsAspect {
    @Pointcut("@annotation(com.vv.finance.investment.bg.annotation.StatisticsCount)")
    public void serviceCount() {
    }

    @Resource
    IStockNewsService stockNewsService;

    /**
     * 调用方法之后
     *
     * @param joinPoint
     */
    @AfterReturning(value = "serviceCount()", returning = "returnValue")
    public void afterMethod(JoinPoint joinPoint, Object returnValue) {
//        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
//        Object[] objects = joinPoint.getArgs();
        if (returnValue == null) {
            return;
        }
        long id = (long) ReflectUtil.getFieldValue(returnValue, "id");
        StockNewsEntity stockNewsEntity = stockNewsService.getById(id);
        if (stockNewsEntity != null) {
            stockNewsEntity.setReadingVolume(stockNewsEntity.getReadingVolume() + 1);
            stockNewsService.updateById(stockNewsEntity);
        }
    }
}
