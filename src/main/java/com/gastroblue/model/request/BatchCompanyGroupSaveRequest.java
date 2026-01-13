package com.gastroblue.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchCompanyGroupSaveRequest(
    @Valid CompanyGroupSaveRequest companyGroup,
    @NotEmpty @Size(max = 5000) @Valid List<@Valid CompanySaveRequest> items) {}
