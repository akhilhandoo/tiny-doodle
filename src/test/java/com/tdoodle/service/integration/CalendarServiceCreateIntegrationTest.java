package com.tdoodle.service.integration;

import com.tdoodle.Application;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.service.TestUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest(classes = {Application.class})
public class CalendarServiceCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    public void cleanup() {
        timeSlotRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    public void createTimeslot_WhenValidRequest_ThenCreateAndAssert() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;

        mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, minutes)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.slotId", notNullValue()))
                .andExpect(jsonPath("$.userId", is(1023)))
                .andExpect(jsonPath("$.beginTime", is(beginTime.toString())))
                .andExpect(jsonPath("$.free", is(true)));

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(1);
        Assertions.assertThat(timeSlot.get(0)).isNotNull();
        Assertions.assertThat(timeSlot.get(0).getUserId()).isEqualTo(1023);
        Assertions.assertThat(timeSlot.get(0).getBeginTime()).isEqualTo(beginTime);
        Assertions.assertThat(timeSlot.get(0).getDurationInMinutes()).isEqualTo(minutes);
        Assertions.assertThat(timeSlot.get(0).getFree()).isEqualTo(true);
    }

    @SneakyThrows
    @Test
    public void createTimeslot_WhenInvalidBeginTime_ThenBadRequest() {

        // Given
        var beginTime = Instant.now().minus(1, ChronoUnit.DAYS);
        var minutes = 30;

        mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, minutes)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Begin time is in the past.")));

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(0);
    }

    @SneakyThrows
    @Test
    public void createTimeslot_WhenInvalidDuration_ThenBadRequest() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 0;

        mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, minutes)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("duration cannot be less than 1 minute.")));

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(0);
    }

    @SneakyThrows
    @Test
    public void createTimeslot_WhenTimeSlotOverlapsExactlyWithExisting_ThenReturnHttpConflict() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;

        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(beginTime);
        timeslot.setDurationInMinutes(minutes);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, minutes)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Timeslot overlaps with an existing one.")));

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(1);
    }

    @SneakyThrows
    @Test
    public void createTimeslot_WhenTimeSlotOverlapsPartiallyWithExisting_ThenReturnHttpConflict() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;

        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(beginTime);
        timeslot.setDurationInMinutes(minutes);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime.plus(25, ChronoUnit.MINUTES), minutes)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Timeslot overlaps with an existing one.")));

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(1);
    }

    @SneakyThrows
    @Test
    public void postSeveralConcurrentOverlappingTimeslotRequests_ThenValidateSingleCreation() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;

        makeConcurrentRestCallsAndValidateResponse(beginTime, minutes, 10, 1);

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).hasSize(1);
        Assertions.assertThat(timeSlot.get(0)).isNotNull();
        Assertions.assertThat(timeSlot.get(0).getUserId()).isEqualTo(1023);
        Assertions.assertThat(timeSlot.get(0).getDurationInMinutes()).isEqualTo(minutes);
        Assertions.assertThat(timeSlot.get(0).getFree()).isEqualTo(true);
    }

    private void makeConcurrentRestCallsAndValidateResponse(
            Instant beginTime, int minutes, int n, long awaitSeconds) throws InterruptedException {

        var executorService = Executors.newFixedThreadPool(n);

        for (int index = 0; index < n; index++) {
            final int delta = index;
            executorService.submit(
                    () -> {
                        try {
                            mockMvc.perform(post("/tdoodle/users/1023/time-slots").contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime.plus(delta, ChronoUnit.MINUTES), minutes)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
        executorService.shutdown();
        executorService.awaitTermination(awaitSeconds, TimeUnit.SECONDS);
    }
}
