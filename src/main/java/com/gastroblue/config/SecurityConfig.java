package com.gastroblue.config;

import com.gastroblue.model.enums.ApplicationRole;
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
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {

    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize -> {

              // 🔓 Public endpoints
              authorize
                  .requestMatchers(
                      "/api/v1/auth/login", "/api/v1/auth/refresh", "/actuator/health/**")
                  .permitAll();

              // 📘 Swagger (env controlled)
              if (swaggerEnabled) {
                authorize
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll();
              }
              // 🔐 ADMIN only
              authorize
                  .requestMatchers(
                      "/api/v1/definition/company-groups", "/api/v1/definition/company-groups/**")
                  .hasRole(ApplicationRole.ADMIN.name());

              // 🔒 Everything else
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
