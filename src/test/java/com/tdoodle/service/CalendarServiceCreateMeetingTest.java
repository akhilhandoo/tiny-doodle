package com.tdoodle.service;

import com.tdoodle.exception.ConflictException;
import com.tdoodle.exception.NotFoundException;
import com.tdoodle.persistence.MeetingRepository;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.Meeting;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.representation.CreateMeetingRequest;
import com.tdoodle.representation.MeetingResponse;
import com.tdoodle.service.mapper.MeetingMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CalendarServiceCreateMeetingTest {

  @Mock private TimeSlotRepository timeSlotRepository;

  @Mock private MeetingRepository meetingRepository;

  @Mock private MeetingMapper meetingMapper;

  @InjectMocks private CalendarService calendarService;

  @Test
  void shouldCreateMeetingWhenValidRequest() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var userId = 23;
    var timeSlotId = 1L;

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(true);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));

    var meetingId = 1L;
    var title = "Some meeting title";
    var description = "Some meeting description";
    var participants = List.of("person 1", "person 2", "person 3");

    var meeting = new Meeting();
    meeting.setTimeSlot(timeSlotWithId);
    meeting.setTitle(title);
    meeting.setDescription(description);
    meeting.setParticipants(participants);

    var meetingWithId = new Meeting();
    meetingWithId.setMeetingId(meetingId);
    meetingWithId.setTimeSlot(timeSlotWithId);
    meetingWithId.setTitle(title);
    meetingWithId.setDescription(description);
    meetingWithId.setParticipants(participants);

    Mockito.when(meetingRepository.save(meeting)).thenReturn(meetingWithId);

    var meetingTimeSlot = new TimeSlot();
    meetingTimeSlot.setSlotId(timeSlotId);
    meetingTimeSlot.setUserId(userId);
    meetingTimeSlot.setBeginTime(beginTime);
    meetingTimeSlot.setDurationInMinutes(durationInMinutes);
    meetingTimeSlot.setFree(false);
    meetingTimeSlot.setMeeting(meetingWithId);

    Mockito.when(timeSlotRepository.save(meetingTimeSlot)).thenReturn(meetingTimeSlot);

    var meetingResponse =
        new MeetingResponse(meetingId, timeSlotId, userId, title, description, participants);
    Mockito.when(meetingMapper.toMeetingResponse(meetingWithId)).thenReturn(meetingResponse);

    //  When
    var meetingResponseActual =
        calendarService.createMeeting(
            userId, timeSlotId, new CreateMeetingRequest(title, description, participants));

    // Then
    Assertions.assertEquals(meetingResponse, meetingResponseActual);
  }

  @Test
  void shouldThrowExceptionWhenNonExistentTimeSlotIdInRequest() {
    //  Given
    var userId = 23;
    var timeSlotId = 1L;
    var title = "Some meeting title";
    var description = "Some meeting description";
    var participants = List.of("person 1", "person 2", "person 3");

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

    // When... Then
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            calendarService.createMeeting(
                userId, timeSlotId, new CreateMeetingRequest(title, description, participants)));
  }

  @Test
  void shouldThrowExceptionWhenInvalidUserId() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var userId = 23;
    var timeSlotId = 1L;
    var title = "Some meeting title";
    var description = "Some meeting description";
    var participants = List.of("person 1", "person 2", "person 3");

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(true);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));

    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            calendarService.createMeeting(
                24, timeSlotId, new CreateMeetingRequest(title, description, participants)));
  }

  @Test
  void shouldThrowExceptionWhenSlotIsAMeeting() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var userId = 23;
    var timeSlotId = 1L;
    var title = "Some meeting title";
    var description = "Some meeting description";
    var participants = List.of("person 1", "person 2", "person 3");

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(false);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));

    Assertions.assertThrows(
        ConflictException.class,
        () ->
            calendarService.createMeeting(
                userId, timeSlotId, new CreateMeetingRequest(title, description, participants)));
  }
}
