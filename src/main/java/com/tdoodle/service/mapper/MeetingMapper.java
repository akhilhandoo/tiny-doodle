package com.tdoodle.service.mapper;

import com.tdoodle.config.MapperConfig;
import com.tdoodle.persistence.entity.Meeting;
import com.tdoodle.representation.MeetingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface MeetingMapper {

    @Mapping(target = "timeSlotId", source = "meeting.timeSlot.slotId")
    @Mapping(target = "userId", source = "meeting.timeSlot.userId")
    MeetingResponse toMeetingResponse(Meeting meeting);
}