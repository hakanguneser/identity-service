package com.gastroblue.model.response;

import com.gastroblue.model.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationPropertyResponse {
  private String propertyId;
  private String propertyKey;
  private String propertyValue;
  private Language language;
  private String description;
}
