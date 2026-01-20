package com.gastroblue.facade;

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
    HashMap<String, Object> extraClaims = getExtraClaims(userEntity, loginRequest);
    String token = jwtService.generateToken(userEntity, extraClaims);
    return AuthLoginResponse.builder()
        .token(token)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .termsAcceptanceRequired(userEntity.isTermsAcceptanceRequired())
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

  public void signAgreement() {
    userDefinitionService.signAgreement(IJwtService.findSessionUserOrThrow().userId());
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
              companyGroup.isThermometerTrackerEnabled(),
              companyGroup.getThermometerTrackerApiUrl(),
              companyGroup.getThermometerTrackerApiVersion());
      case FORMFLOW ->
          buildApiInfo(
              companyGroup.isFormflowEnabled(),
              companyGroup.getFormflowApiUrl(),
              companyGroup.getFormflowApiVersion());
      case ADMIN_PANEL -> ApiInfoDto.builder().build();
    };
  }

  private ApiInfoDto buildApiInfo(boolean enabled, String url, String version) {
    if (!enabled || url == null) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }

    return ApiInfoDto.builder().url(url).version(version).build();
  }

  private HashMap<String, Object> getExtraClaims(
      UserEntity sessionUser, AuthLoginRequest loginRequest) {
    HashMap<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("companyGroupId", sessionUser.getCompanyGroupId());
    extraClaims.put("applicationRole", sessionUser.getApplicationRole());
    extraClaims.put("companyIds", getResponsibleCompanyIds(sessionUser));
    extraClaims.put("iss", issuer);
    extraClaims.put("aud", loginRequest.product());
    extraClaims.put("channel", loginRequest.channel());
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
    if (userEntity.getPasswordValidUntil() == null
        || userEntity.getPasswordValidUntil().isBefore(LocalDateTime.now())) {
      userEntity.setPasswordChangeRequired(true);
    }
    userService.save(userEntity);
  }
}
