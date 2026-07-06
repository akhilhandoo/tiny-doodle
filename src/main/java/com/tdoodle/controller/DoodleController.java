package com.tdoodle.controller;

import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tdoodle")
public class DoodleController {

    private final CalendarService calendarService;

    @GetMapping(value = "/users/{userId}/time-slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlots(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(calendarService.getTimeSlots(userId));
    }

    @PostMapping(value = "/users/{userId}/time-slots", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @PathVariable("userId") Integer userId,
            @RequestBody CreateOrUpdateTimeSlotRequest createTimeSlotRequest) {
        return ResponseEntity.ok(calendarService.createTimeSlot(userId, createTimeSlotRequest));
    }
}