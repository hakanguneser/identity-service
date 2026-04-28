package com.gastroblue.model.response;

import io.gastroblue.commons.shared.enums.Language;
import lombok.Builder;

@Builder
public record ErrorMessageResponse(
    String id, String errorCode, Language language, String message) {}
