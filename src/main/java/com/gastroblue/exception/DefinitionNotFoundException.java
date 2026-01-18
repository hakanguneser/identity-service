package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;

public class DefinitionNotFoundException extends AbstractRuntimeException {

  public DefinitionNotFoundException(String message) {
    super(ErrorCode.DEFINITION_NOT_FOUND, message);
  }
}
