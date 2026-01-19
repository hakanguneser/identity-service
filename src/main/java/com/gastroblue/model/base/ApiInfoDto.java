package com.gastroblue.model.base;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiInfoDto {
  private String url;
  private String version;
}
