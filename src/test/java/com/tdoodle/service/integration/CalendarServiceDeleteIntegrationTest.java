package com.tdoodle.service.integration;

import com.tdoodle.Application;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest(classes = {Application.class})
public class CalendarServiceDeleteIntegrationTest {

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
    public void deleteTimeslot_WhenValidRequest_ThenCreateAndAssert() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(delete("/tdoodle/users/1023/time-slots/{slotId}", timeslot.getSlotId()))
                .andExpect(status().isNoContent());

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).isEmpty();
    }

    @SneakyThrows
    @Test
    public void deleteTimeslot_WhenInvalidSlotId_ReturnNotFound() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(delete("/tdoodle/users/1023/time-slots/{slotId}", -15))
                .andExpect(status().isNotFound());

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).isNotEmpty();
    }

    @SneakyThrows
    @Test
    public void deleteTimeslot_WhenInvalidUserId_ReturnNotFound() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeslot = timeSlotRepository.save(timeslot);

        mockMvc.perform(delete("/tdoodle/users/1024/time-slots/{slotId}", timeslot.getSlotId()))
                .andExpect(status().isNotFound());

        var timeSlot = timeSlotRepository.getAllByUserId(1023);
        Assertions.assertThat(timeSlot).isNotEmpty();
    }
}
