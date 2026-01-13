package com.gastroblue.model.response;

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
}
