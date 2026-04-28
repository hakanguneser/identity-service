package com.gastroblue.config;

import io.gastroblue.commons.shared.enums.ApplicationRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Value("${app.swagger.enabled}")
  private boolean swaggerEnabled;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize -> {
              authorize
                  .requestMatchers(
                      "/api/v1/auth/login", "/api/v1/auth/refresh", "/actuator/health/**")
                  .permitAll();
              if (swaggerEnabled) {
                authorize
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll();
              }
              authorize
                  .requestMatchers("/api/v1/definition/company-groups/**")
                  .hasAnyRole(
                      ApplicationRole.ADMIN.name(),
                      ApplicationRole.APP_CLIENT.name(),
                      ApplicationRole.GROUP_MANAGER.name(),
                      ApplicationRole.ZONE_MANAGER.name());
              authorize
                  .requestMatchers("/api/v1/definition/company-groups/context")
                  .hasAnyRole(ApplicationRole.APP_CLIENT.name());
              authorize
                  .requestMatchers("/api/v1/tracker/**")
                  .hasRole(ApplicationRole.APP_CLIENT.name());
              authorize.anyRequest().authenticated();
            })
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
