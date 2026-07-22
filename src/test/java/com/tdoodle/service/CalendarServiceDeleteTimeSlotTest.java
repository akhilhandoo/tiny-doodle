package com.tdoodle.service;

import com.tdoodle.exception.ConflictException;
import com.tdoodle.exception.NotFoundException;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
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
public class CalendarServiceDeleteTimeSlotTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void shouldNotThrowExceptionWhenDeleteTimeSlotValidRequest() {

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

        // When... Then
        Assertions.assertDoesNotThrow(() -> calendarService.deleteTimeSlot(userId, timeSlotId));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTimeSlot() {

        //  Given
        var userId = 23;
        var timeSlotId = 1L;

        Mockito.when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // When... Then
        Assertions.assertThrows(NotFoundException.class, () -> calendarService.deleteTimeSlot(userId, timeSlotId));
    }

    @Test
    void shouldThrowExceptionWhenDeletingTimeSlotOfAnotherUser() {

        //  Given
        var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var durationInMinutes = 30;
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
        Assertions.assertThrows(NotFoundException.class, () -> calendarService.deleteTimeSlot(nonExistentUserId, timeSlotId));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonFreeTimeSlot() {

        //  Given
        var beginTime = Instant.now().plus(1, ChronoUnit.HOURS);
        var durationInMinutes = 30;
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
        Assertions.assertThrows(ConflictException.class, () -> calendarService.deleteTimeSlot(userId, timeSlotId));
    }
}
