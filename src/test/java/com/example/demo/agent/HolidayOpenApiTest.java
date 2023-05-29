package com.example.demo.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("공휴일 오픈 API 테스트")
class HolidayOpenApiTest {

    @Autowired
    HolidayProperties holidayProperties;

    @Autowired
    HolidayAgent holidayAgent;

    @Test
    void Test_AuthToken() {
        String api = holidayProperties.getApi();
        String authToken = holidayProperties.getAuthToken();
        Assertions.assertTrue(StringUtils.hasText(api));
        Assertions.assertTrue(StringUtils.hasText(authToken));

        Optional<List<Holiday>> holidays = holidayAgent.getHolidaysFromCurrent();
        Assertions.assertTrue(holidays.isPresent());

        List<Holiday> holidayList = holidays.get();
        Assertions.assertFalse(holidayList.isEmpty());

        System.out.println(holidayList.size());
    }
}
