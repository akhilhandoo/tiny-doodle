package com.tdoodle.representation;

import java.util.List;

public record MeetingResponse(
    Long meetingId,
    Long timeSlotId,
    Integer userId,
    String title,
    String description,
    List<String> participants) {}
