package com.tdoodle.representation;

import java.util.List;
import lombok.NonNull;

public record CreateMeetingRequest(
    @NonNull String title, @NonNull String description, @NonNull List<String> participants) {}
