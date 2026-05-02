package com.gastroblue.config;

import com.gastroblue.commons.helper.security.config.AbstractSecurityConfig;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends AbstractSecurityConfig {

  private final AuthenticationProvider authenticationProvider;

  @Override
  protected void configureAuthorization(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          auth) {
    auth.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll();
    auth.requestMatchers("/api/v1/definition/company-groups/**")
        .hasAnyRole(
            ApplicationRole.ADMIN.name(),
            ApplicationRole.APP_CLIENT.name(),
            ApplicationRole.GROUP_MANAGER.name(),
            ApplicationRole.ZONE_MANAGER.name());
    auth.requestMatchers("/api/v1/definition/company-groups/context")
        .hasAnyRole(ApplicationRole.APP_CLIENT.name());
    auth.requestMatchers("/api/v1/tracker/**").hasRole(ApplicationRole.APP_CLIENT.name());
    auth.anyRequest().authenticated();
  }

  @Override
  protected void configureAdditional(HttpSecurity http) throws Exception {
    http.authenticationProvider(authenticationProvider);
  }
}
