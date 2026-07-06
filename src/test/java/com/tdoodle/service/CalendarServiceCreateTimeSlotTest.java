package com.tdoodle.service;

import com.tdoodle.exception.TimeSlotInPastException;
import com.tdoodle.exception.TimeSlotOverlapException;
import com.tdoodle.exception.TimeSlotTooShortException;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.mapper.TimeSlotMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CalendarServiceCreateTimeSlotTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private TimeSlotMapper timeSlotMapper;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void shouldCreateTimeSlotWhenValidRequest() {

        //  Given
        var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var durationInMinutes = 30;
        var endTime = beginTime.plus(durationInMinutes, ChronoUnit.MINUTES);
        var userId = 23;

        var timeSlot = new TimeSlot();
        timeSlot.setUserId(userId);
        timeSlot.setBeginTime(beginTime);
        timeSlot.setFree(true);
        timeSlot.setDurationInMinutes(durationInMinutes);

        var timeSlotWithId = new TimeSlot();
        timeSlotWithId.setSlotId(1L);
        timeSlotWithId.setUserId(userId);
        timeSlotWithId.setBeginTime(beginTime);
        timeSlotWithId.setFree(true);
        timeSlotWithId.setDurationInMinutes(durationInMinutes);

        var timeSlotResponse = new TimeSlotResponse(timeSlotWithId.getSlotId(), timeSlotWithId.getUserId(), timeSlotWithId.getBeginTime(), timeSlotWithId.getDurationInMinutes(), timeSlotWithId.getFree());

        Mockito.when(timeSlotRepository.getCountOfOverlappingTimeSlotsByUser(userId, beginTime, endTime)).thenReturn(0);
        Mockito.when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlotWithId);
        Mockito.when(timeSlotMapper.toTimeSlotResponse(timeSlotWithId)).thenReturn(timeSlotResponse);

        //  When
        var timeSlotResponseActual = calendarService.createTimeSlot(userId, new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes));

        // Then
        Assertions.assertEquals(timeSlotResponse, timeSlotResponseActual);
    }

    @Test
    void shouldThrowExceptionWhenTimeSlotInThePast() {

        //  Given
        var beginTime = Instant.now().minus(1, ChronoUnit.HOURS);
        var durationInMinutes = 30;
        var userId = 23;

        // When.. Then
        Assertions.assertThrows(TimeSlotInPastException.class, () -> calendarService.createTimeSlot(userId, new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
    }

    @Test
    void shouldThrowExceptionWhenNegativeDuration() {

        //  Given
        var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var durationInMinutes = -5;
        var userId = 23;

        // When.. Then
        Assertions.assertThrows(TimeSlotTooShortException.class, () -> calendarService.createTimeSlot(userId, new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
    }

    @Test
    void shouldThrowExceptionWhenOverlappingTimeSlots() {

        //  Given
        var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var durationInMinutes = 30;
        var endTime = beginTime.plus(durationInMinutes, ChronoUnit.MINUTES);
        var userId = 23;

        Mockito.when(timeSlotRepository.getCountOfOverlappingTimeSlotsByUser(userId, beginTime, endTime)).thenReturn(1);

        // When.. Then
        Assertions.assertThrows(TimeSlotOverlapException.class, () -> calendarService.createTimeSlot(userId, new CreateOrUpdateTimeSlotRequest(beginTime, durationInMinutes)));
    }
}
