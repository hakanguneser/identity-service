package com.gastroblue.exception;


import com.gastroblue.exception.base.AbstractRuntimeException;
import com.gastroblue.model.enums.ErrorCode;

public class AccessDeniedException extends AbstractRuntimeException {

    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
