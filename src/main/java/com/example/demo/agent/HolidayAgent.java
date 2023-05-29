package com.example.demo.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * [한국천문연구원_특일 정보]
 * - <a href="https://www.data.go.kr/data/15012690/openapi.do">Link</a>
 */
@Slf4j
@Component
public class HolidayAgent {

    private static final TypeReference<List<Holiday>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private final HolidayProperties holidayProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public HolidayAgent(HolidayProperties holidayProperties) {
        this.holidayProperties = holidayProperties;
        this.webClient = WebClient.builder()
                .baseUrl(holidayProperties.getApi())
                .build();
        this.objectMapper = Jackson2ObjectMapperBuilder.json().build();
    }

    public Optional<List<Holiday>> getHolidaysFromCurrent() {
        String currentYear = String.valueOf(Instant.now().atZone(ZoneId.of("Asia/Seoul")).getYear());
        return getHolidaysByYear(currentYear);
    }

    public Optional<List<Holiday>> getHolidaysByYear(String yyyy) {
        List<Holiday> holidayList = new ArrayList<>();

        ResponseEntity<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/getRestDeInfo")
                        .queryParam("serviceKey", holidayProperties.getAuthToken())
                        .queryParam("solYear", yyyy)
                        .queryParam("numOfRows", 100) // NOTE: 법정공휴일은 평균 70일 정도
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .toEntity(String.class)
                .block(Duration.ofMinutes(3));

        if (response == null) {
            return Optional.empty();
        }

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            return Optional.empty();
        }

        // NOTE: 게이트웨이 방식의 서버 오류 시 XML 응답 가능성이 있음.
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.contains("OpenAPI_ServiceResponse") || !responseBody.contains("response")) {
            log.warn("[Holiday] API Error. \n{}", responseBody);
            Message message = new Message();
            message.setMessage("Holiday API Error");

            SentryEvent sentryEvent = new SentryEvent();
            sentryEvent.setMessage(message);
            sentryEvent.setLevel(SentryLevel.ERROR);
            Sentry.captureEvent(sentryEvent);
            return Optional.empty();
        }

        try {
            JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(responseBody);
            JSONArray itemArr = JsonPath.parse(jsonObject.toJSONString()).read("$.response.body.items.item[*]");
            List<Holiday> holidays = objectMapper.readValue(itemArr.toJSONString(), TYPE_REFERENCE);
            return Optional.of(holidays);
        } catch (ParseException e) {
            log.warn("[Holiday] JSON parse error.", e);
            Sentry.captureException(e);
        } catch (JsonProcessingException e) {
            log.warn("[Holiday] JSON processing error.", e);
            Sentry.captureException(e);
        }

        return Optional.of(holidayList);
    }

}
