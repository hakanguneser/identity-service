package com.gastroblue.model.request;

import com.gastroblue.commons.shared.enums.Language;
import java.time.LocalDate;

public record CompanyGroupEulaContentUpdateRequest(
    String eulaVersion,
    Language language,
    String content,
    LocalDate startDate,
    LocalDate endDate) {}
