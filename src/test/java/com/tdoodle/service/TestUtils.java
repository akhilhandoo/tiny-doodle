package com.tdoodle.service;

import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String createTimeSlotRequestJson(Instant beginTime, int durationInMinutes) {
        return objectMapper.writeValueAsString(new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes));
    }
}
