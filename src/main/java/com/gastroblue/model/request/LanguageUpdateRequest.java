package com.gastroblue.model.request;

import com.gastroblue.commons.shared.enums.Language;
import jakarta.validation.constraints.NotNull;

public record LanguageUpdateRequest(@NotNull Language language) {}
