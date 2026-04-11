package com.gastroblue.model.response;

import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Language;
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
