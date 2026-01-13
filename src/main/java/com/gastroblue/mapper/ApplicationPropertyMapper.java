package com.gastroblue.mapper;

import com.gastroblue.model.entity.ApplicationPropertyEntity;
import com.gastroblue.model.request.ApplicationPropertySaveRequest;
import com.gastroblue.model.response.ApplicationPropertyResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationPropertyMapper {

  public static ApplicationPropertyEntity toEntity(final ApplicationPropertySaveRequest request) {
    return ApplicationPropertyEntity.builder()
        .propertyKey(request.propertyKey())
        .language(request.language())
        .propertyValue(request.propertyValue())
        .description(request.description())
        .build();
  }

  public static ApplicationPropertyResponse toResponse(final ApplicationPropertyEntity entity) {
    return ApplicationPropertyResponse.builder()
        .propertyId(entity.getId())
        .propertyKey(entity.getPropertyKey())
        .propertyValue(entity.getPropertyValue())
        .language(entity.getLanguage())
        .description(entity.getDescription())
        .build();
  }
}
