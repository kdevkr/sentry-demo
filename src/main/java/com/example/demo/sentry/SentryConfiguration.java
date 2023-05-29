package com.example.demo.sentry;

import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.spring.jakarta.SentryTaskDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods = false)
public class SentryConfiguration {

    @Bean
    public SentryTaskDecorator sentryTaskDecorator() {
        return new SentryTaskDecorator();
    }
    @Bean
    public Sentry.OptionsConfiguration<SentryOptions> custom(@Autowired(required = false) BuildProperties buildProperties) {
        return options -> {
            if (buildProperties != null) {
                options.setRelease(buildProperties.getVersion());
            } else {
                options.setRelease("v1.0.0");
            }
        };
    }

    @Component
    public static class CustomBeforeSendCallback implements SentryOptions.BeforeSendCallback {
        @Override
        public SentryEvent execute(SentryEvent event, Hint hint) {
            // Example: Never send server name in events
            event.setServerName(null);
            return event;
        }
    }
}
