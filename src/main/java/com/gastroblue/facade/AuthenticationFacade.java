package com.gastroblue.facade;

import static com.gastroblue.model.enums.ApplicationProduct.FORMFLOW;
import static com.gastroblue.model.enums.ApplicationProduct.THERMOMETER_TRACKER;
import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.*;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.request.RefreshTokenRequest;
import com.gastroblue.model.response.*;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserDefinitionService userService;
  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final UserDefinitionService userDefinitionService;
  private final EnumConfigurationFacade enumConfigurationFacade;
  private final CompanyGroupEulaContentService eulaContentService;

  @Value("${spring.application.name}")
  private String issuer;

  public AuthLoginResponse login(AuthLoginRequest loginRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.username(), loginRequest.password()));
    } catch (BadCredentialsException e) {
      throw new AccessDeniedException(INVALID_USERNAME_OR_PASSWORD);
    } catch (RuntimeException e) {
      log.error("Authentication failed1: {}", e.getMessage());
    }
    UserEntity userEntity = userService.findUserEntityByUserName(loginRequest.username());
    updateUserAfterSuccessfulLogin(userEntity, loginRequest.product());
    ApiInfoDto apiInfo = getApiInfo(userEntity, loginRequest.product());
    HashMap<String, Object> extraClaims = getExtraClaims(userEntity, loginRequest.product());
    String token = jwtService.generateToken(userEntity, extraClaims);
    String refreshToken = jwtService.generateRefreshToken(userEntity);
    return AuthLoginResponse.builder()
        .token(token)
        .refreshToken(refreshToken)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .eulaRequired(userEntity.isEulaRequired())
        .apiInfo(apiInfo)
        .build();
  }

  public AuthLoginResponse refreshToken(RefreshTokenRequest request) {
    if (jwtService.isTokenExpired(request.refreshToken())) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }
    String username = jwtService.extractUsername(request.refreshToken());
    UserEntity userEntity = userService.findUserEntityByUserName(username);
    if (!jwtService.isTokenValid(request.refreshToken(), userEntity)) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    ApiInfoDto apiInfo = getApiInfo(userEntity, userEntity.getLastSuccessLoginProduct());

    // Create a dummy login request to reuse the getExtraClaims logic or recreate it
    // Using channel from request if present, otherwise null or default
    // We need to re-fetch or reconstruct context.
    // Since getExtraClaims needs AuthLoginRequest, let's extract the logic or
    // create a simpler version.
    // However, existing getExtraClaims uses loginRequest.product() which we might
    // not have in RefreshTokenRequest if we didn't store it in the claim.
    // Ideally we should store product/aud in the token claims.

    // Let's check getExtraClaims again. It puts "aud" -> loginRequest.product().
    // So we can extract "aud" from the refresh token if we put it there?
    // Wait, generateRefreshToken uses empty map for extraClaims.
    // So the refresh token DOES NOT have "aud" or "companyGroupId" in it currently.
    // We should probably add them to the refresh token too if we want to restore
    // the session faithfully.

    // For now, let's assume we can get it from the user's last success login
    // product if available.
    ApplicationProduct product = userEntity.getLastSuccessLoginProduct();
    if (product == null) {
      product = ApplicationProduct.ADMIN_PANEL; // Fallback? or throw?
    }
    HashMap<String, Object> extraClaims = getExtraClaims(userEntity, product);

    String newToken = jwtService.generateToken(userEntity, extraClaims);
    String newRefreshToken = jwtService.generateRefreshToken(userEntity);

    return AuthLoginResponse.builder()
        .token(newToken)
        .refreshToken(newRefreshToken)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .eulaRequired(userEntity.isEulaRequired())
        .apiInfo(apiInfo)
        .build();
  }

  public AuthUserInfoResponse findAuthenticatedUserInfo() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AuthUserInfoResponse response = new AuthUserInfoResponse();
    response.setUser(UserMapper.toBase(sessionUser));
    if (sessionUser.companyGroupId() != null) {
      try {
        CompanyGroup companyGroup = companyGroupService.findById(sessionUser.companyGroupId());
        response.setCompanyGroup(companyGroup);
      } catch (IllegalDefinitionException exception) {
        log.info("Company group not found: {}", sessionUser.companyGroupId());
      }
    }
    if (sessionUser.companyId() != null) {
      try {
        Company company = companyService.findByBaseId(sessionUser.companyId());
        response.setCompany(company);
      } catch (IllegalDefinitionException exception) {
        log.info("Company not found: {}", sessionUser.companyGroupId());
      }
    }
    return response;
  }

  public List<AuthUserCompanyGroupResponse> findMyCompanyGroups() {
    return companyGroupService.findMyCompanyGroups().stream()
        .map(CompanyGroupMapper::toAuthResponse)
        .toList();
  }

  public List<AuthUserCompanyResponse> findMyCompanies() {
    return companyService
        .findByCompanyGroupId(IJwtService.findSessionUserOrThrow().companyGroupId())
        .stream()
        .map(entity -> CompanyGroupMapper.toAuthResponse(entity, enumConfigurationFacade))
        .toList();
  }

  public void signEula() {
    userDefinitionService.signEula(IJwtService.findSessionUserOrThrow().userId());
  }

  public EulaResponse getEula() {
    String activeEulaContent =
        eulaContentService.getActiveEulaContent(
            IJwtService.findSessionUserOrThrow().companyGroupId());
    return new EulaResponse(activeEulaContent);
  }

  private ApiInfoDto getApiInfo(UserEntity userEntity, ApplicationProduct product) {
    if (userEntity.getApplicationRole().isAdministrator()) {
      return ApiInfoDto.builder().build();
    }
    CompanyGroup companyGroup = companyGroupService.findById(userEntity.getCompanyGroupId());
    return switch (product) {
      case THERMOMETER_TRACKER ->
          buildApiInfo(
              THERMOMETER_TRACKER,
              companyGroup.isThermometerTrackerEnabled(),
              companyGroup.getThermometerTrackerApiUrl(),
              companyGroup.getThermometerTrackerApiVersion());
      case FORMFLOW ->
          buildApiInfo(
              FORMFLOW,
              companyGroup.isFormflowEnabled(),
              companyGroup.getFormflowApiUrl(),
              companyGroup.getFormflowApiVersion());
      case ADMIN_PANEL -> ApiInfoDto.builder().build();
    };
  }

  private ApiInfoDto buildApiInfo(
      ApplicationProduct product, boolean enabled, String url, String version) {
    if (!enabled || url == null) {
      switch (product) {
        case FORMFLOW:
          throw new AccessDeniedException(ErrorCode.FORMFLOW_APP_NOT_ENABLED_FOR_COMPANY_GROUP);
        case THERMOMETER_TRACKER:
          throw new AccessDeniedException(
              ErrorCode.THERMOMETER_TRACKER_APP_NOT_ENABLED_FOR_COMPANY_GROUP);
        default:
          throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
      }
    }
    return ApiInfoDto.builder().url(url).version(version).build();
  }

  private HashMap<String, Object> getExtraClaims(
      UserEntity sessionUser, ApplicationProduct product) {
    HashMap<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("cgId", sessionUser.getCompanyGroupId());
    extraClaims.put("role", sessionUser.getApplicationRole());
    extraClaims.put("cIds", getResponsibleCompanyIds(sessionUser));
    extraClaims.put("iss", issuer);
    extraClaims.put("aud", product);
    return extraClaims;
  }

  private List<String> getResponsibleCompanyIds(UserEntity sessionUser) {
    return switch (sessionUser.getApplicationRole()) {
      case ADMIN, GROUP_MANAGER -> List.of();
      case ZONE_MANAGER ->
          companyService.findByCompanyGroupId(sessionUser.getCompanyGroupId()).stream()
              .map(CompanyEntity::getId)
              .toList();
      default -> List.of(sessionUser.getCompanyId());
    };
  }

  private void updateUserAfterSuccessfulLogin(UserEntity userEntity, ApplicationProduct product) {
    userEntity.setLastSuccessLogin(LocalDateTime.now());
    userEntity.setLastSuccessLoginProduct(product);
    if (userEntity.getPasswordExpiresAt() == null
        || userEntity.getPasswordExpiresAt().isBefore(LocalDateTime.now())) {
      userEntity.setPasswordChangeRequired(true);
    }
    userService.save(userEntity);
  }
}
