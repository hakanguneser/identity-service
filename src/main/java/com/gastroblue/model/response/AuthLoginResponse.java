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
  private String refreshToken;
  private boolean passwordChangeRequired;
  private boolean eulaRequired;
  private ApiInfoDto apiInfo;
}
