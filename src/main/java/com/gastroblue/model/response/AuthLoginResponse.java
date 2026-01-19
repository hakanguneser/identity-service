package com.gastroblue.model.response;

import com.gastroblue.model.base.ApiInfoDto;
import lombok.*;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Builder
public class AuthLoginResponse {
  private String token;
  private boolean passwordChangeRequired;
  private boolean termsAcceptanceRequired;
  private ApiInfoDto apiInfo;
}
