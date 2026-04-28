package com.gastroblue.model.request;

import io.gastroblue.commons.shared.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ErrorMessageSaveRequest(
    @NotNull String errorCode, @NotNull Language language, @NotBlank String message) {}
