package com.gastroblue.model.request;

import jakarta.validation.constraints.NotNull;

public record CompanyGroupProductUpdateRequest(
    @NotNull(message = "{validation.enabled.check.null}") Boolean enabled,
    String apiUrl,
    String apiVersion,
    String notes) {}
