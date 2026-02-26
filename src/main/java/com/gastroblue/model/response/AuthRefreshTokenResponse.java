package com.gastroblue.model.response;

import lombok.*;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Builder
public class AuthRefreshTokenResponse {
  private String token;
}
