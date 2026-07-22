package com.tdoodle.controller;

import com.tdoodle.representation.CreateMeetingRequest;
import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import com.tdoodle.representation.MeetingResponse;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tdoodle")
public class DoodleController {

  private final CalendarService calendarService;

  @GetMapping(value = "/users/{userId}/time-slots", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<TimeSlotResponse>> getTimeSlots(
      @PathVariable("userId") Integer userId,
      @RequestParam Map<String, String> queryParams) {
    return ResponseEntity.ok(calendarService.getTimeSlots(userId, queryParams));
  }

  @PostMapping(
      value = "/users/{userId}/time-slots",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TimeSlotResponse> createTimeSlot(
      @PathVariable("userId") Integer userId,
      @Valid @RequestBody CreateOrUpdateTimeSlotRequest createTimeSlotRequest) {
    return ResponseEntity.ok(calendarService.createTimeSlot(userId, createTimeSlotRequest));
  }

  @DeleteMapping(
      value = "/users/{userId}/time-slots/{timeSlotId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteTimeSlot(
      @PathVariable("userId") Integer userId, @PathVariable("timeSlotId") Long timeSlotId) {
    calendarService.deleteTimeSlot(userId, timeSlotId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping(
      value = "/users/{userId}/time-slots/{timeSlotId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TimeSlotResponse> updateTimeSlot(
      @PathVariable("userId") Integer userId,
      @PathVariable("timeSlotId") Long timeSlotId,
      @RequestBody CreateOrUpdateTimeSlotRequest updateTimeSlotRequest) {
    return ResponseEntity.ok(
        calendarService.updateTimeSlot(userId, timeSlotId, updateTimeSlotRequest));
  }

  @PostMapping(
      value = "/users/{userId}/time-slots/{timeSlotId}/meetings",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MeetingResponse> createMeeting(
      @PathVariable("userId") Integer userId,
      @PathVariable("timeSlotId") Long timeSlotId,
      @RequestBody CreateMeetingRequest createMeetingRequest) {
    return ResponseEntity.ok(
        calendarService.createMeeting(userId, timeSlotId, createMeetingRequest));
  }
}
