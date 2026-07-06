package com.tdoodle.representation;

import lombok.NonNull;

import java.time.Instant;

public record CreateOrUpdateTimeSlotRequest(@NonNull Instant beginTime, @NonNull Integer durationInMinutes) {}
