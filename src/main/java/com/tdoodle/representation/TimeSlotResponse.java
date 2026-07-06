package com.tdoodle.representation;

import java.time.Instant;

public record TimeSlotResponse(
    Long slotId, Integer userId, Instant beginTime, Integer durationInMinutes, Boolean free) {}
