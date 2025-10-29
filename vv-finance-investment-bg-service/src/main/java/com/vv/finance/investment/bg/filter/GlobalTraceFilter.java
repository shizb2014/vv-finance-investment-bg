package com.vv.finance.investment.bg.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @Description 过滤器传递tradeId（消费者和生产者）
 * @Author liuxing
 * @Date 2022/10/5 15:24
 * @Version v1.0
 */
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER})
public class GlobalTraceFilter implements Filter {
    private static final String TRACE_ID = "TraceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext rpcContext = RpcContext.getContext();
        String traceId;
        if (rpcContext.isConsumerSide()) {
            traceId = MDC.get(TRACE_ID);
            if (traceId == null) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            rpcContext.setAttachment(TRACE_ID, traceId);
        }else if (rpcContext.isProviderSide()) {
            traceId = rpcContext.getAttachment(TRACE_ID);
            if (traceId == null) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(TRACE_ID, traceId);
        }
        return invoker.invoke(invocation);

//        String traceId = invocation.getAttachment(TRACE_ID);
//        System.out.println(traceId);
//        if(!StringUtils.isBlank(traceId)) {
//            RpcContext.getContext().setAttachment(TRACE_ID,traceId);
//        }else { // 第一次发起调用
//            RpcContext.getContext().setAttachment(TRACE_ID, UUID.randomUUID().toString());
//        }
//        return invoker.invoke(invocation);
    }
}
