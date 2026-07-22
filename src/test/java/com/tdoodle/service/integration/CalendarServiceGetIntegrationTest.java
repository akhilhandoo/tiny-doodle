package com.tdoodle.service.integration;

import com.tdoodle.Application;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.TimeSlot;
import lombok.SneakyThrows;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest(classes = {Application.class})
public class CalendarServiceGetIntegrationTest {

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
    public void getSingleTimeslot() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        mockMvc.perform(get("/tdoodle/users/1023/time-slots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeslot1 = new TimeSlot();
        timeslot1.setUserId(1023);
        timeslot1.setBeginTime(Instant.now().plus(2, ChronoUnit.DAYS));
        timeslot1.setDurationInMinutes(30);
        timeslot1.setFree(true);
        timeSlotRepository.save(timeslot1);

        var timeslot2 = new TimeSlot();
        timeslot2.setUserId(1023);
        timeslot2.setBeginTime(Instant.now().plus(3, ChronoUnit.DAYS));
        timeslot2.setDurationInMinutes(30);
        timeslot2.setFree(true);
        timeSlotRepository.save(timeslot2);

        mockMvc.perform(get("/tdoodle/users/1023/time-slots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @SneakyThrows
    @Test
    public void getSingleTimeslot_WhenInvalidUserId_ThenReturn_EmptyList() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        mockMvc.perform(get("/tdoodle/users/1024/time-slots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots_WhenBusyFilter_ReturnAppropriateResponse() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeslot1 = new TimeSlot();
        timeslot1.setUserId(1023);
        timeslot1.setBeginTime(Instant.now().plus(2, ChronoUnit.DAYS));
        timeslot1.setDurationInMinutes(30);
        timeslot1.setFree(false);
        timeSlotRepository.save(timeslot1);

        var timeslot2 = new TimeSlot();
        timeslot2.setUserId(1023);
        timeslot2.setBeginTime(Instant.now().plus(3, ChronoUnit.DAYS));
        timeslot2.setDurationInMinutes(30);
        timeslot2.setFree(true);
        timeSlotRepository.save(timeslot2);

        mockMvc.perform(get("/tdoodle/users/1023/time-slots?slotType=BUSY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots_WhenFreeFilter_ReturnAppropriateResponse() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeslot1 = new TimeSlot();
        timeslot1.setUserId(1023);
        timeslot1.setBeginTime(Instant.now().plus(2, ChronoUnit.DAYS));
        timeslot1.setDurationInMinutes(30);
        timeslot1.setFree(false);
        timeSlotRepository.save(timeslot1);

        var timeslot2 = new TimeSlot();
        timeslot2.setUserId(1023);
        timeslot2.setBeginTime(Instant.now().plus(3, ChronoUnit.DAYS));
        timeslot2.setDurationInMinutes(30);
        timeslot2.setFree(true);
        timeSlotRepository.save(timeslot2);

        mockMvc.perform(get("/tdoodle/users/1023/time-slots?slotType=FREE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots_WhenInvalidFilter_ReturnBadRequest() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeslot1 = new TimeSlot();
        timeslot1.setUserId(1023);
        timeslot1.setBeginTime(Instant.now().plus(2, ChronoUnit.DAYS));
        timeslot1.setDurationInMinutes(30);
        timeslot1.setFree(false);
        timeSlotRepository.save(timeslot1);

        var timeslot2 = new TimeSlot();
        timeslot2.setUserId(1023);
        timeslot2.setBeginTime(Instant.now().plus(3, ChronoUnit.DAYS));
        timeslot2.setDurationInMinutes(30);
        timeslot2.setFree(true);
        timeSlotRepository.save(timeslot2);

        mockMvc.perform(get("/tdoodle/users/1023/time-slots?slotType=Free"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Invalid filter for timeSlotType: Free in request.")));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots_WhenTimeFrameFilter_ThenAppropriateResponse() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeslot1 = new TimeSlot();
        timeslot1.setUserId(1023);
        timeslot1.setBeginTime(Instant.now().plus(2, ChronoUnit.DAYS));
        timeslot1.setDurationInMinutes(30);
        timeslot1.setFree(true);
        timeSlotRepository.save(timeslot1);

        var timeslot2 = new TimeSlot();
        timeslot2.setUserId(1023);
        timeslot2.setBeginTime(Instant.now().plus(3, ChronoUnit.DAYS));
        timeslot2.setDurationInMinutes(30);
        timeslot2.setFree(true);
        timeSlotRepository.save(timeslot2);

        var timeslot3 = new TimeSlot();
        timeslot3.setUserId(1023);
        timeslot3.setBeginTime(Instant.now().plus(4, ChronoUnit.DAYS));
        timeslot3.setDurationInMinutes(30);
        timeslot3.setFree(true);
        timeSlotRepository.save(timeslot3);

        var timeFrameBegin = Instant.now().plus(2, ChronoUnit.DAYS).toString();
        var timeFrameEnd = Instant.now().plus(3, ChronoUnit.DAYS).toString();

        mockMvc.perform(get("/tdoodle/users/1023/time-slots?timeFrameBegin={beginTime}&timeFrameEnd={endTime}", timeFrameBegin, timeFrameEnd))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @SneakyThrows
    @Test
    public void getSeveralTimeSlots_WhenPartialTimeFrameFilter_ReturnBadRequest() {

        // Given
        var timeslot = new TimeSlot();
        timeslot.setUserId(1023);
        timeslot.setBeginTime(Instant.now().plus(1, ChronoUnit.DAYS));
        timeslot.setDurationInMinutes(30);
        timeslot.setFree(true);
        timeSlotRepository.save(timeslot);

        var timeFrameBegin = Instant.now().plus(2, ChronoUnit.DAYS).toString();

        mockMvc.perform(get("/tdoodle/users/1023/time-slots?timeFrameBegin={beginTime}", timeFrameBegin))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Both timeFrameBegin and timeFrameEnd are required together or not at all.")));
    }
}
