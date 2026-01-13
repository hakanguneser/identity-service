package com.gastroblue.model.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUserDefinitionResponse {
  private List<UserDefinitionResponse> userDefinitions;
}
