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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CalendarService {

  private static final String TIME_FRAME_BEGIN_PARAMETER = "timeFrameBegin";
  private static final String TIME_FRAME_END_PARAMETER = "timeFrameEnd";
  private static final Set<String> GET_QUERY_DATE_FILTERS = Set.of(TIME_FRAME_BEGIN_PARAMETER, TIME_FRAME_END_PARAMETER);
  private final TimeSlotRepository timeSlotRepository;
  private final MeetingRepository meetingRepository;
  private final TimeSlotMapper timeSlotMapper;
  private final MeetingMapper meetingMapper;

  public List<TimeSlotResponse> getTimeSlots(Integer userId, Map<String, String> queryParams) {

    var timeFrameBegin = getTimeFrameValues(queryParams, TIME_FRAME_BEGIN_PARAMETER);
    var timeFrameEnd = getTimeFrameValues(queryParams, TIME_FRAME_END_PARAMETER);
    if (!timeFrameBegin.isEmpty() || !timeFrameEnd.isEmpty()) {
      if (timeFrameBegin.isEmpty() || timeFrameEnd.isEmpty()) {
        throw new BusinessValidationException("Both timeFrameBegin and timeFrameEnd are required together or not at all.");
      }
    }
    var timeSlots = timeFrameBegin.isEmpty() ? timeSlotRepository.getAllByUserId(userId) : timeSlotRepository.getAllIntersectingTimeSlotsByUser(userId, timeFrameBegin.get(), timeFrameEnd.get());
    var slotTypeToUse = determineSlotTypeQuery(queryParams);
    if (!slotTypeToUse.isEmpty()) {
      if (TimeSlotType.FREE.equals(slotTypeToUse.get())) {
        timeSlots = timeSlots.stream().filter(timeSlot -> timeSlot.getFree()).collect(Collectors.toList());
      } else {
        timeSlots = timeSlots.stream().filter(timeSlot -> !timeSlot.getFree()).collect(Collectors.toList());
      }
    }
    return timeSlots.stream().map(timeSlotMapper::toTimeSlotResponse).collect(Collectors.toList());
  }

  private Optional<Instant> getTimeFrameValues(Map<String, String> queryParams, String key) {
    try {
      if (!GET_QUERY_DATE_FILTERS.contains(key)) {
        throw new BusinessValidationException("Invalid query parameter: " + key + " in request.");
      }
      return Optional.ofNullable(queryParams.get(key)).map(Instant::parse);
    } catch (DateTimeParseException e) {
      throw new BusinessValidationException("Invalid date format in request filter.");
    }
  }

  private Optional<TimeSlotType> determineSlotTypeQuery(Map<String, String> queryParams) {
    var slotTypeValue = queryParams.get("slotType");
    if (null == slotTypeValue) {
      return Optional.empty();
    }
    try {
      return Optional.of(Enum.valueOf(TimeSlotType.class, slotTypeValue));
    } catch (IllegalArgumentException ie) {
      throw new BusinessValidationException("Invalid filter for timeSlotType: " + slotTypeValue + " in request.");
    } catch (NullPointerException npe) {
      Optional.empty();
    }
    return Optional.empty();
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
