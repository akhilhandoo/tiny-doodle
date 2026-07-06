package com.tdoodle.exception;

public class TimeSlotInPastException extends BusinessValidationException {
  public TimeSlotInPastException(String message) {
    super(message);
  }
}
