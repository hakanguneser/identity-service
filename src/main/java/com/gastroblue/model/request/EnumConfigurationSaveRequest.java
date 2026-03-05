package com.gastroblue.model.request;

import com.gastroblue.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EnumConfigurationSaveRequest(
    @NotBlank String companyGroupId,
    @NotBlank String enumType,
    @NotBlank String enumKey,
    @NotNull Language language,
    @NotBlank String label,
    boolean active,
    Integer displayOrder) {}
