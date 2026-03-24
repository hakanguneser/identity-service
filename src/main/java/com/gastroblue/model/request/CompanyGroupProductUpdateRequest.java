package com.gastroblue.model.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CompanyGroupProductUpdateRequest(
    @NotNull(message = "{validation.enabled.check.null}") Boolean enabled,
    LocalDate licenseExpiresAt,
    Integer agreedUserCount,
    String apiUrl,
    String apiVersion,
    String notes) {}
