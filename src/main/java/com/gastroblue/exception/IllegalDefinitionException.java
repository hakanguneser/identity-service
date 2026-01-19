package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IllegalDefinitionException extends AbstractRuntimeException {
  public IllegalDefinitionException() {
    super(ErrorCode.ILLEGAL_DEFINITION);
  }

  public IllegalDefinitionException(String message) {
    super(ErrorCode.ILLEGAL_DEFINITION, message);
  }
}
