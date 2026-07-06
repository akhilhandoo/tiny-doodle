package com.tdoodle.exception;

public class TimeSlotTooShortException extends BusinessValidationException {
  public TimeSlotTooShortException(String message) {
    super(message);
  }
}
