package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IllegalDefinitionException extends AbstractRuntimeException {

  public IllegalDefinitionException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
