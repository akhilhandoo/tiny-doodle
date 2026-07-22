package com.tdoodle.representation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateOrUpdateTimeSlotRequest(
        @NotNull(message = "begin-time cannot be null.") Instant beginTime, @NotNull(message = "duration cannot be null.") @Min(value = 1, message = "duration cannot be less than 1 minute.") Integer durationInMinutes) {}
