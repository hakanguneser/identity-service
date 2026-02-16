package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class DefinitionNotFoundException extends AbstractRuntimeException {

  public DefinitionNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
