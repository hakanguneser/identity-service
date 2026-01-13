package com.gastroblue.exception;

import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.DefinitionType;
import com.gastroblue.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class IllegalDefinitionException extends AbstractRuntimeException {

  private final DefinitionType definitionType;

  public IllegalDefinitionException(DefinitionType definitionType) {
    super(ErrorCode.ILLEGAL_DEFINITION);
    this.definitionType = definitionType;
  }
}
