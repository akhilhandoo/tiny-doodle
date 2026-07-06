package com.tdoodle.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "time_slot")
public class TimeSlot {

  @Id
  @Column(name = "slot_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long slotId;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "begin_time")
  private Instant beginTime;

  @Column(name = "duration_in_minutes")
  private Integer durationInMinutes;

  private Boolean free;

  @OneToOne(fetch = FetchType.EAGER, mappedBy = "timeSlot")
  @PrimaryKeyJoinColumn
  private Meeting meeting;
}
