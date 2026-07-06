package com.tdoodle.service;


import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CalendarService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;

    public List<TimeSlotResponse> getTimeSlots(Integer userId) {
        return timeSlotRepository.getAllByUserId(userId).stream().map(timeSlot -> timeSlotMapper.toTimeSlotResponse(timeSlot)).collect(Collectors.toList());
    }
}
