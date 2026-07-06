package com.tdoodle.service;

import com.tdoodle.exception.*;
import com.tdoodle.persistence.MeetingRepository;
import com.tdoodle.persistence.TimeSlotRepository;
import com.tdoodle.persistence.entity.Meeting;
import com.tdoodle.persistence.entity.TimeSlot;
import com.tdoodle.representation.CreateMeetingRequest;
import com.tdoodle.representation.CreateOrUpdateTimeSlotRequest;
import com.tdoodle.representation.MeetingResponse;
import com.tdoodle.representation.TimeSlotResponse;
import com.tdoodle.service.mapper.MeetingMapper;
import com.tdoodle.service.mapper.TimeSlotMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CalendarService {

  private final TimeSlotRepository timeSlotRepository;
  private final MeetingRepository meetingRepository;
  private final TimeSlotMapper timeSlotMapper;
  private final MeetingMapper meetingMapper;

  public List<TimeSlotResponse> getTimeSlots(Integer userId) {
    return timeSlotRepository.getAllByUserId(userId).stream()
        .map(timeSlot -> timeSlotMapper.toTimeSlotResponse(timeSlot))
        .collect(Collectors.toList());
  }

  @Transactional
  public TimeSlotResponse createTimeSlot(
      @NonNull Integer userId, @NonNull CreateOrUpdateTimeSlotRequest request) {
    validateCreateOrUpdateTimeSlotRequest(request);
    int overlappingTimeSlotsCount =
        timeSlotRepository.getCountOfOverlappingTimeSlotsByUser(
            userId,
            request.beginTime(),
            request.beginTime().plus(request.durationInMinutes(), ChronoUnit.MINUTES));
    if (overlappingTimeSlotsCount > 0) {
      throw new TimeSlotOverlapException("Timeslot overlaps with an existing one.");
    }
    var timeSlot = new TimeSlot();
    timeSlot.setUserId(userId);
    timeSlot.setBeginTime(request.beginTime());
    timeSlot.setFree(true);
    timeSlot.setDurationInMinutes(request.durationInMinutes());
    return timeSlotMapper.toTimeSlotResponse(timeSlotRepository.save(timeSlot));
  }

  private void validateCreateOrUpdateTimeSlotRequest(CreateOrUpdateTimeSlotRequest request) {
    if (request.beginTime().isBefore(Instant.now())) {
      throw new TimeSlotInPastException("Begin time is in the past.");
    }
    if (request.durationInMinutes() <= 0) {
      throw new TimeSlotTooShortException("Duration is invalid.");
    }
  }

  @Transactional
  public void deleteTimeSlot(@NonNull Integer userId, @NonNull Long timeSlotId) {
    var timeSlot =
        timeSlotRepository
            .findById(timeSlotId)
            .orElseThrow(() -> new NotFoundException("Given time-slot was not found."));
    validateChangeOfTimeSlotRequest(timeSlot, userId);
    timeSlotRepository.delete(timeSlot);
  }

  //  Check if a given time-slot actually belongs to the said user.
  //  And check if that time-slot is free(not a meeting) to be modified.
  private void validateChangeOfTimeSlotRequest(TimeSlot timeSlot, Integer userId) {
    if (!timeSlot.getUserId().equals(userId)) {
      throw new NotFoundException("Given time-slot was not found.");
    }
    if (!timeSlot.getFree()) {
      throw new ConflictException("Given time-slot is a meeting. Request meeting update/delete.");
    }
  }

  @Transactional
  public TimeSlotResponse updateTimeSlot(
      @NonNull Integer userId,
      @NonNull Long timeSlotId,
      @NonNull CreateOrUpdateTimeSlotRequest updatedTimeSlotRequest) {
    validateCreateOrUpdateTimeSlotRequest(updatedTimeSlotRequest);
    var timeSlot =
        timeSlotRepository
            .findById(timeSlotId)
            .orElseThrow(() -> new NotFoundException("Given time-slot was not found."));
    validateChangeOfTimeSlotRequest(timeSlot, userId);
    int overlappingTimeSlotsCount =
        timeSlotRepository.getCountOfOverlappingTimeSlotsByUserExceptGivenTimeSlot(
            userId,
            updatedTimeSlotRequest.beginTime(),
            updatedTimeSlotRequest
                .beginTime()
                .plus(updatedTimeSlotRequest.durationInMinutes(), ChronoUnit.MINUTES),
            timeSlotId);
    if (overlappingTimeSlotsCount > 0) {
      throw new TimeSlotOverlapException("Timeslot overlaps with an existing one.");
    }
    timeSlot.setDurationInMinutes(updatedTimeSlotRequest.durationInMinutes());
    timeSlot.setBeginTime(updatedTimeSlotRequest.beginTime());
    return timeSlotMapper.toTimeSlotResponse(timeSlotRepository.save(timeSlot));
  }

  @Transactional
  public MeetingResponse createMeeting(
      @NonNull Integer userId,
      @NonNull Long timeSlotId,
      CreateMeetingRequest createMeetingRequest) {
    var timeSlot =
        timeSlotRepository
            .findById(timeSlotId)
            .orElseThrow(() -> new NotFoundException("Given time-slot was not found."));
    validateChangeOfTimeSlotRequest(timeSlot, userId);
    var meeting = new Meeting();
    meeting.setTimeSlot(timeSlot);
    meeting.setTitle(createMeetingRequest.title());
    meeting.setDescription(createMeetingRequest.description());
    meeting.setParticipants(createMeetingRequest.participants());
    meeting = meetingRepository.save(meeting);
    timeSlot.setMeeting(meeting);
    timeSlot.setFree(false);
    timeSlotRepository.save(timeSlot);
    return meetingMapper.toMeetingResponse(meeting);
  }
}
