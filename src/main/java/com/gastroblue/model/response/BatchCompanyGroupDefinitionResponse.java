package com.gastroblue.model.response;

import lombok.*;

import java.util.List;

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
