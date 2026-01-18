package com.gastroblue.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyContextResponse {
  private CompanyGroupDefinitionResponse companyGroup;
  private CompanyDefinitionResponse company;
}
