package com.vv.finance.investment.bg.filter;

import com.vv.finance.base.exception.BusinessRuntimeException;
import com.vv.finance.investment.gateway.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.service.GenericService;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * Dubbo异常过滤器
 * <p>
 * order设置比原生异常大，保证在漏去除原生异常时比原生异常晚执行，结果早返回
 *
 * @author Liam
 * @version 1.0
 * @date 2020/10/10 11:20
 */
@Slf4j
@Activate(group = PROVIDER, order = 1)
public class DubboExceptionFilter extends ExceptionFilter {

    private final Logger logger;

    public DubboExceptionFilter() {
        this(LoggerFactory.getLogger(DubboExceptionFilter.class));
    }

    public DubboExceptionFilter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onResponse(
        Result result,
        Invoker<?> invoker,
        Invocation invocation
    ) {
        if (result.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                Throwable exception = result.getException();

                logErrorWithArgs(invoker, invocation, exception);

                // 为自定义异常时直接返回结果
                if (exception instanceof BusinessRuntimeException) {
                    return;
                }

                // sso 定义的异常为自定义异常时直接返回结果
                if (exception instanceof com.vv.finance.investment.exception.BusinessRuntimeException) {
                    return;
                }

                // 如果为RuntimeException直接返回，避免其他微服务未去掉原生exception导致进入onResponse重复判断，
                // 虽然源码在判断异常文件和接口再同一包时，RuntimeException获取到的异常文件为空同时会返回，此处直接显示返回
                if (exception instanceof RuntimeException) {
                    return;
                }

                // 其他情况使用Dubbo原生的异常处理
                super.onResponse(result, invoker, invocation);
            } catch (Throwable e) {
                log.warn("Fail to DubboExceptionFilter when called by " + RpcContext.getContext().getRemoteHost()
                    + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                    + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
                return;
            }
        }
        return;
    }

    private void logErrorWithArgs(
        Invoker<?> invoker,
        Invocation invocation,
        Throwable exception
    ) {
        String arguments = null;
        try {
            arguments = JsonUtils.beanToJson(invocation.getArguments());
        } catch (Exception e) {
            arguments = "parse argument error";
        }

        log.error(
            "DubboExceptionFilter: " + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface()
                .getName() + ", method: " + invocation.getMethodName() + "\n, args: " + arguments + "\n, exception: "
                + exception.getClass().getName() + ": " + exception.getMessage(), exception);
    }

}
