package com.vv.finance.investment.bg.aspectj;

import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.exception.BusinessRuntimeException;
import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @ClassName: IndexParamAspectj
 * @Description: 指数参数校验
 * @Author: Demon
 * @Datetime: 2020/11/2   15:33
 */
@Aspect
@Component
public class IndexParamAspectj {


    @Pointcut("execution(* com.vv.finance.investment.bg.api.impl.index.*.query*(..))")
    public void pointCut() {
    }

    @Before(value = "pointCut()")
    public void beforePointCut(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length <= 0) {
            throw new BusinessRuntimeException(ResultCode.FAIL, "req.param.error");
        }

        Object param = args[0];
        if (param instanceof IndexQueryReq) {
            IndexQueryReq req = (IndexQueryReq) args[0];
            if (StringUtils.isEmpty(req.getCode())){
                throw new BusinessRuntimeException(ResultCode.FAIL, "req.param.error");
            }
        }

        if (param instanceof String){
            if (StringUtils.isEmpty(param.toString())){
                throw new BusinessRuntimeException(ResultCode.FAIL, "req.param.error");
            }
        }
    }

}
