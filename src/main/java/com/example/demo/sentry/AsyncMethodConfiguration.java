package com.example.demo.sentry;

import io.sentry.spring.jakarta.SentryTaskDecorator;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncMethodConfiguration implements AsyncConfigurer {

    private final SentryTaskDecorator sentryTaskDecorator;

    public AsyncMethodConfiguration(SentryTaskDecorator sentryTaskDecorator) {
        this.sentryTaskDecorator = sentryTaskDecorator;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(sentryTaskDecorator);
        executor.initialize();
        return executor;
    }
}