package com.gastroblue.exception.base;

import com.gastroblue.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class AbstractRuntimeException extends RuntimeException {
  private final ErrorCode errorCode;

  protected AbstractRuntimeException(ErrorCode errorCode) {
    super(errorCode.name());
    this.errorCode = errorCode;
  }
}
