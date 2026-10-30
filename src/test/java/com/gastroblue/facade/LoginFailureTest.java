package com.gastroblue.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.security.model.properties.JwtProperties;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ChannelType;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.service.CompanyGroupEulaContentService;
import com.gastroblue.service.CompanyGroupProductService;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyProductService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.TokenGenerationService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

class LoginFailureTest {

  private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
  private final UserDefinitionService userService = mock(UserDefinitionService.class);

  private final AuthenticationFacade facade =
      new AuthenticationFacade(
          mock(IJwtService.class),
          mock(JwtProperties.class),
          mock(ILookupService.class),
          mock(TokenGenerationService.class),
          authenticationManager,
          userService,
          mock(CompanyService.class),
          mock(CompanyGroupService.class),
          mock(CompanyGroupEulaContentService.class),
          mock(CompanyGroupProductService.class),
          mock(CompanyProductService.class),
          mock(UserProductService.class));

  @ParameterizedTest
  @ValueSource(strings = {"disabled", "locked", "expired", "credentials", "password"})
  void everyRejectedLoginGetsTheSameAnswer(String reason) {
    when(authenticationManager.authenticate(any())).thenThrow(failure(reason));

    assertThatThrownBy(
            () ->
                facade.login(
                    new AuthLoginRequest(
                        "user", "secret", ChannelType.MOBILE, ApplicationProduct.TRACKER)))
        .isInstanceOfSatisfying(
            AccessDeniedException.class,
            e ->
                assertThat(e.getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_USERNAME_OR_PASSWORD.code()));
    if (!"password".equals(reason)) {
      // Only a wrong password counts towards the lock.
      verify(userService, never()).incrementLoginAttempts(any());
    }
  }

  private static AuthenticationException failure(String reason) {
    return switch (reason) {
      case "disabled" -> new DisabledException("disabled");
      case "locked" -> new LockedException("locked");
      case "expired" -> new AccountExpiredException("expired");
      case "credentials" -> new CredentialsExpiredException("expired");
      default -> new BadCredentialsException("bad");
    };
  }
}
