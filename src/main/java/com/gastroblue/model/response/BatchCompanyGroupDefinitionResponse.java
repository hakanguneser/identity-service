package com.gastroblue.model.response;

import java.util.List;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCompanyGroupDefinitionResponse {
  CompanyGroupDefinitionResponse companyGroup;
  List<CompanyDefinitionResponse> companies;
}
