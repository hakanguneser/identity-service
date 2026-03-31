package com.gastroblue.model.request;

import com.gastroblue.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ErrorMessageSaveRequest(
    @NotNull String errorCode, @NotNull Language language, @NotBlank String message) {}
