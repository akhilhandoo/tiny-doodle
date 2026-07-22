package com.tdoodle.exception;

public class BusinessValidationException extends ClientErrorException {
  public BusinessValidationException(String message) {
    super(message);
  }
}
