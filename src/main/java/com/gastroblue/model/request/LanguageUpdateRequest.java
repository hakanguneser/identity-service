package com.gastroblue.model.request;

import io.gastroblue.commons.shared.enums.Language;
import jakarta.validation.constraints.NotNull;

public record LanguageUpdateRequest(@NotNull Language language) {}
