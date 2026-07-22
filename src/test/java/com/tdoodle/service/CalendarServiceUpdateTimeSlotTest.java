package com.tdoodle.service;

import com.tdoodle.exception.ConflictException;
import com.tdoodle.exception.NotFoundException;
import com.tdoodle.exception.TimeSlotInPastException;
import com.tdoodle.exception.TimeSlotTooShortException;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.mapper.TimeSlotMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
public class CalendarServiceUpdateTimeSlotTest {

  @Mock private TimeSlotRepository timeSlotRepository;

  @Mock private TimeSlotMapper timeSlotMapper;

  @InjectMocks private CalendarService calendarService;

  @Test
  void shouldReturnCorrectResponseWhenUpdateTimeSlotValidRequest() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var updateBeginTime = beginTime.plus(1, ChronoUnit.HOURS);
    var updateEndTime = updateBeginTime.plus(durationInMinutes, ChronoUnit.MINUTES);
    var userId = 23;
    var timeSlotId = 1L;

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(true);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));
    Mockito.when(
            timeSlotRepository.getCountOfOverlappingTimeSlotsByUserExceptGivenTimeSlot(
                userId, updateBeginTime, updateEndTime, timeSlotId))
        .thenReturn(0);

    timeSlotWithId.setBeginTime(updateBeginTime);
    Mockito.when(timeSlotRepository.save(timeSlotWithId)).thenReturn(timeSlotWithId);

    var timeSlotResponse =
        new TimeSlotResponse(
            timeSlotId,
            timeSlotWithId.getUserId(),
            timeSlotWithId.getBeginTime(),
            timeSlotWithId.getDurationInMinutes(),
            timeSlotWithId.getFree());
    Mockito.when(timeSlotMapper.toTimeSlotResponse(timeSlotWithId)).thenReturn(timeSlotResponse);

    // When
    var timeSlotResponseActual =
        calendarService.updateTimeSlot(
            userId,
            timeSlotId,
            new CreateOrUpdateTimeSlotRequest(updateBeginTime, durationInMinutes));

    // Then
    Assertions.assertEquals(timeSlotResponse, timeSlotResponseActual);
  }

  @Test
  void shouldThrowExceptionWhenInvalidBeginTimeInRequest() {
    //  Given
    var beginTime = Instant.now().minus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var userId = 23;
    var timeSlotId = 1L;

    // When... Then
    Assertions.assertThrows(
        TimeSlotInPastException.class,
        () ->
            calendarService.updateTimeSlot(
                userId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
  }

  @Test
  void shouldThrowExceptionWhenInvalidDurationInRequest() {
    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 0;
    var userId = 23;
    var timeSlotId = 1L;

    // When... Then
    Assertions.assertThrows(
        TimeSlotTooShortException.class,
        () ->
            calendarService.updateTimeSlot(
                userId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
  }

  @Test
  void shouldThrowExceptionWhenNonExistentTimeSlotIdInRequest() {
    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 10;
    var userId = 23;
    var timeSlotId = 1L;

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

    // When... Then
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            calendarService.updateTimeSlot(
                userId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingTimeSlotOfAnotherUser() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var updateBeginTime = beginTime.plus(1, ChronoUnit.HOURS);
    var userId = 23;
    var nonExistentUserId = 24;
    var timeSlotId = 1L;

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(true);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));

    // When... Then
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            calendarService.updateTimeSlot(
                nonExistentUserId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(updateBeginTime, durationInMinutes)));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonFreeTimeSlot() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var updateBeginTime = beginTime.plus(1, ChronoUnit.HOURS);
    var userId = 23;
    var timeSlotId = 1L;

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(false);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));

    // When... Then
    Assertions.assertThrows(
        ConflictException.class,
        () ->
            calendarService.updateTimeSlot(
                userId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(updateBeginTime, durationInMinutes)));
  }

  @Test
  void shouldThrowExceptionWhenOverlappingTimeSlots() {

    //  Given
    var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
    var durationInMinutes = 30;
    var updateBeginTime = beginTime.plus(1, ChronoUnit.HOURS);
    var updateEndTime = updateBeginTime.plus(durationInMinutes, ChronoUnit.MINUTES);
    var userId = 23;
    var timeSlotId = 1L;

    var timeSlotWithId = new TimeSlot();
    timeSlotWithId.setSlotId(timeSlotId);
    timeSlotWithId.setUserId(userId);
    timeSlotWithId.setBeginTime(beginTime);
    timeSlotWithId.setFree(true);
    timeSlotWithId.setDurationInMinutes(durationInMinutes);

    Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlotWithId));
    Mockito.when(
            timeSlotRepository.getCountOfOverlappingTimeSlotsByUserExceptGivenTimeSlot(
                userId, updateBeginTime, updateEndTime, timeSlotId))
        .thenReturn(2);

    // When... Then
    Assertions.assertThrows(
        ConflictException.class,
        () ->
            calendarService.updateTimeSlot(
                userId,
                timeSlotId,
                new CreateOrUpdateTimeSlotRequest(updateBeginTime, durationInMinutes)));
  }
}
