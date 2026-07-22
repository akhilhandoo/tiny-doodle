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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest(classes = {Application.class})
public class CalendarServiceUpdateIntegrationTest {

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
    public void updateTimeslot_WhenValidRequest_ThenCreateAndAssert() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;
        var updatedMinutes = 45;
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(beginTime);
        timeslot.setDurationInMinutes(minutes);
        timeslot.setFree(true);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(put("/tdoodle/users/1023/time-slots/{timeslotId}", timeslot.getSlotId()).contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, updatedMinutes)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.slotId", is(timeslot.getSlotId().intValue())))
                .andExpect(jsonPath("$.userId", is(1023)))
                .andExpect(jsonPath("$.beginTime", is(beginTime.toString())))
                .andExpect(jsonPath("$.durationInMinutes", is(updatedMinutes)))
                .andExpect(jsonPath("$.free", is(true)));

        var timeslotFromRepo = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeslotFromRepo).hasSize(1);
        Assertions.assertThat(timeslotFromRepo.get(0)).isNotNull();
        Assertions.assertThat(timeslotFromRepo.get(0).getUserId()).isEqualTo(1023);
        Assertions.assertThat(timeslotFromRepo.get(0).getBeginTime()).isEqualTo(beginTime);
        Assertions.assertThat(timeslotFromRepo.get(0).getDurationInMinutes()).isEqualTo(updatedMinutes);
        Assertions.assertThat(timeslotFromRepo.get(0).getFree()).isEqualTo(true);
    }

    @SneakyThrows
    @Test
    public void updateTimeslot_WhenSlotIsBusy_ThenReturnConflict() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;
        var updatedMinutes = 45;
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(beginTime);
        timeslot.setDurationInMinutes(minutes);
        timeslot.setFree(false);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(put("/tdoodle/users/1023/time-slots/{timeslotId}", timeslot.getSlotId()).contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, updatedMinutes)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Given time-slot is a meeting. Request meeting update/delete.")));

        var timeslotFromRepo = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeslotFromRepo).hasSize(1);
        Assertions.assertThat(timeslotFromRepo.get(0)).isNotNull();
        Assertions.assertThat(timeslotFromRepo.get(0).getUserId()).isEqualTo(1023);
        Assertions.assertThat(timeslotFromRepo.get(0).getBeginTime()).isEqualTo(beginTime);
        Assertions.assertThat(timeslotFromRepo.get(0).getDurationInMinutes()).isEqualTo(minutes);
        Assertions.assertThat(timeslotFromRepo.get(0).getFree()).isEqualTo(false);
    }

    @SneakyThrows
    @Test
    public void updateTimeslot_WhenSlotOwnedByAnotherUser_ThenReturnNotFound() {

        // Given
        var beginTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var minutes = 30;
        var updatedMinutes = 45;
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(beginTime);
        timeslot.setDurationInMinutes(minutes);
        timeslot.setFree(false);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(put("/tdoodle/users/1024/time-slots/{timeslotId}", timeslot.getSlotId()).contentType(MediaType.APPLICATION_JSON).content(TestUtils.createTimeSlotRequestJson(beginTime, updatedMinutes)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Given time-slot was not found.")));

        var timeslotFromRepo = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeslotFromRepo).hasSize(1);
        Assertions.assertThat(timeslotFromRepo.get(0)).isNotNull();
        Assertions.assertThat(timeslotFromRepo.get(0).getUserId()).isEqualTo(1023);
        Assertions.assertThat(timeslotFromRepo.get(0).getBeginTime()).isEqualTo(beginTime);
        Assertions.assertThat(timeslotFromRepo.get(0).getDurationInMinutes()).isEqualTo(minutes);
        Assertions.assertThat(timeslotFromRepo.get(0).getFree()).isEqualTo(false);
    }
}
