package com.tdoodle.controller;

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
}