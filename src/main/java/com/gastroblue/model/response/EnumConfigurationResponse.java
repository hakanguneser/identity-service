package com.gastroblue.model.response;

import io.gastroblue.commons.shared.enums.ApplicationProduct;
import io.gastroblue.commons.shared.enums.Language;
import lombok.Builder;

@Builder
public record EnumConfigurationResponse(
    String id,
    String companyGroupId,
    String enumType,
    String enumKey,
    Language language,
    String label,
    boolean active,
    Integer displayOrder,
    String parentKey,
    String parentEnumType,
    ApplicationProduct product) {}
