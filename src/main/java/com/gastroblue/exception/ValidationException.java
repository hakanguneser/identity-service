package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;

public class ValidationException extends AbstractRuntimeException {

  public ValidationException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
