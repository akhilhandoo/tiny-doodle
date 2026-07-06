package com.tdoodle.service.mapper;

import com.tdoodle.config.MapperConfig;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.representation.TimeSlotResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface TimeSlotMapper {
  TimeSlotResponse toTimeSlotResponse(TimeSlot timeSlot);
}
