package com.example.rest.springbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchConfig {

    @Bean(name = "jobStepTaskExecuter")
    public TaskExecutor jobStepTaskExecuter() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // adjust as needed
        executor.setMaxPoolSize(8);  // adjust as needed
        executor.setQueueCapacity(100); // adjust as needed
        executor.setThreadNamePrefix("JobStep-");
        executor.initialize();
        return executor;
    }
}