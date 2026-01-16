package com.gastroblue.model.response;

import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import lombok.Builder;

@Builder
public record ErrorMessageResponse(
    String id, ErrorCode errorCode, Language language, String message) {}
