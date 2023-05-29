package com.example.demo.agent;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "holiday")
@Component
@Getter
@Setter
public class HolidayProperties {
    private String api;
    private String authToken;
}
