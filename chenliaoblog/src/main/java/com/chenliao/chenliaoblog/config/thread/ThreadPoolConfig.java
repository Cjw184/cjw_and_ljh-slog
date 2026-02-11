package com.chenliao.chenliaoblog.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {
     /* 邮件发送专用线程池
     */
    @Bean("mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数（根据CPU核心数配置，建议2-4）
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(4);
        // 队列容量（核心线程满了后，任务先入队列）
        executor.setQueueCapacity(100);
        // 线程名称前缀（便于日志排查）
        executor.setThreadNamePrefix("mail-send-");
        // 线程空闲超时时间（60秒）
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：队列满+最大线程数满时，由调用方线程执行（避免任务丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
