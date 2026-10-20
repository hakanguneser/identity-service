package com.gastroblue.model.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyDefinitionListResponse {
  private List<CompanyDefinitionResponse> companies;
}
