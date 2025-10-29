package com.vv.finance.investment.bg.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @ClassName TaskExecutorConfig
 * @Deacription TODO 线程池配置类
 * @Author lh.sz
 * @Date 2020年11月17日 23:03
 **/
@Configuration
@EnableAsync
public class TaskExecutorConfig {
    @Value("${task-executor.core_pool_size}")
    private int corePoolSize;
    @Value("${task-executor.max_pool_size}")
    private int maxPoolSize;
    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;
    @Value("${task-executor.name-prefix}")
    private String namePrefix;

    @Bean("asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //线程池核心线程数
        taskExecutor.setCorePoolSize(corePoolSize);
        //线程池最大线程数
        taskExecutor.setMaxPoolSize(maxPoolSize);
        //缓冲队列
        taskExecutor.setQueueCapacity(queueCapacity);
        //线程名称前缀
        taskExecutor.setThreadNamePrefix(namePrefix);
        //线程空闲后的最大存活时间
        taskExecutor.setKeepAliveSeconds(60);
        //rejection-policy：当pool已经达到max size的时候，如何处理新任务
        //CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        //对拒绝task的处理策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        taskExecutor.initialize();
        return TtlExecutors.getTtlExecutorService(taskExecutor.getThreadPoolExecutor());
    }
    @Bean("jobExecutorService")
    public ExecutorService jobExecutorService(){
        ThreadPoolExecutor jobExecutorService = new ThreadPoolExecutor(10, 20, 0, TimeUnit.SECONDS
                , new LinkedBlockingQueue<>(100000), new CustomizableThreadFactory("jobExecutorService")
        );
        return TtlExecutors.getTtlExecutorService(jobExecutorService);
    }

    @Bean("klineExecutorService")
    public ExecutorService klineExecutorService(){
        ThreadPoolExecutor klineExecutorService = new ThreadPoolExecutor(60, 200, 0, TimeUnit.SECONDS
                , new LinkedBlockingQueue<>(100), new CustomizableThreadFactory("klineExecutorService")
        );
        return TtlExecutors.getTtlExecutorService(klineExecutorService);
    }

    @Bean("brokerExecutorService")
    public Executor brokerExecutorService() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //线程池核心线程数
        taskExecutor.setCorePoolSize(5);
        //线程池最大线程数
        taskExecutor.setMaxPoolSize(10);
        //缓冲队列
        taskExecutor.setQueueCapacity(200);
        //线程名称前缀
        taskExecutor.setThreadNamePrefix("brokerExecutorService");
        //线程空闲后的最大存活时间
        taskExecutor.setKeepAliveSeconds(60);
        //rejection-policy：当pool已经达到max size的时候，如何处理新任务
        //CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        //对拒绝task的处理策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        taskExecutor.initialize();
        return TtlExecutors.getTtlExecutorService(taskExecutor.getThreadPoolExecutor());
    }

}
