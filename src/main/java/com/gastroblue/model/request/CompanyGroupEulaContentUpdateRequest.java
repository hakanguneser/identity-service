package com.gastroblue.model.request;

import com.gastroblue.model.enums.Language;
import java.time.LocalDateTime;

public record CompanyGroupEulaContentUpdateRequest(
    String eulaVersion,
    Language language,
    String content,
    LocalDateTime startDate,
    LocalDateTime endDate) {}
