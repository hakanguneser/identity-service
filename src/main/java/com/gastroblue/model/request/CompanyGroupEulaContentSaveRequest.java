package com.gastroblue.model.request;

import com.gastroblue.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CompanyGroupEulaContentSaveRequest(
    @NotBlank(message = "{validation.eulaVersion.check.blank}") String eulaVersion,
    @NotNull(message = "{validation.language.check.null}") Language language,
    @NotBlank(message = "{validation.content.check.blank}") String content,
    @NotNull(message = "{validation.startDate.check.null}") LocalDate startDate,
    LocalDate endDate) {}
