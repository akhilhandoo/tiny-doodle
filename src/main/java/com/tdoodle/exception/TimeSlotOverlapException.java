package com.tdoodle.exception;

public class TimeSlotOverlapException extends ConflictException {
  public TimeSlotOverlapException(String message) {
    super(message);
  }
}
