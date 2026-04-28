package com.gastroblue.model.response;

import io.gastroblue.commons.shared.enums.ApplicationProduct;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyGroupProductResponse {

  private String id;
  private String companyGroupId;
  private ApplicationProduct product;
  private Boolean enabled;
  private String apiUrl;
  private String apiVersion;
  private String notes;
}
