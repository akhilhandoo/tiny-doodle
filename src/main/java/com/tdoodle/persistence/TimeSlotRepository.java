package com.tdoodle.persistence;

import com.tdoodle.persistence.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

  List<TimeSlot> getAllByUserId(Integer userId);

  @Query(
      value =
          """
                    SELECT count(t.slot_id) FROM time_slot t
                        WHERE t.user_id = :userId and
                        ((t.begin_time > :beginTime and t.begin_time < :endTime) or
                        ((t.begin_time + (t.duration_in_minutes * interval '1 minute')) > :beginTime and (t.begin_time + (t.duration_in_minutes * interval '1 minute')) < :endTime))
            """,
      nativeQuery = true)
  int getCountOfOverlappingTimeSlotsByUser(Integer userId, Instant beginTime, Instant endTime);

  @Query(
      value =
          """
                    SELECT count(t.slot_id) FROM time_slot t
                        WHERE t.slot_id != :timeSlotId and
                        t.user_id = :userId and
                        ((t.begin_time > :beginTime and t.begin_time < :endTime) or
                        ((t.begin_time + (t.duration_in_minutes * interval '1 minute')) > :beginTime and (t.begin_time + (t.duration_in_minutes * interval '1 minute')) < :endTime))
            """,
      nativeQuery = true)
  int getCountOfOverlappingTimeSlotsByUserExceptGivenTimeSlot(
      Integer userId, Instant beginTime, Instant endTime, Long timeSlotId);

  @Query(
          value =
                  """
                            SELECT t.slot_id, t.user_id, t.begin_time, t.duration_in_minutes, t.free FROM time_slot t
                                WHERE t.user_id = :userId and
                                ((t.begin_time between :beginTime and :endTime) or
                                ((t.begin_time + (t.duration_in_minutes * interval '1 minute')) between :beginTime and :endTime))
                    """,
          nativeQuery = true)
  List<TimeSlot> getAllIntersectingTimeSlotsByUser(Integer userId, Instant beginTime, Instant endTime);
}
