package com.gastroblue.model.response;

import com.gastroblue.model.enums.Language;
import lombok.Builder;

@Builder
public record ErrorMessageResponse(
    String id, String errorCode, Language language, String message) {}
