package com.gastroblue.facade;

import static com.gastroblue.model.enums.ApplicationProduct.FORMFLOW;
import static com.gastroblue.model.enums.ApplicationProduct.THERMOMETER_TRACKER;
import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;
import static com.gastroblue.service.IJwtService.*;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mail.IMailService;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
  private final IMailService mailService;

  @Value("${application.security.jwt.token-validity-in-minutes}")
  private Long jwtTokenValidityMinutes;

  @Value("${application.security.jwt.refresh-token-validity-in-days}")
  private Long jwtRefreshTokenValidityDays;

  public AuthLoginResponse login(AuthLoginRequest loginRequest) {
    // mailService.sendMail(List.of("hakan.guneser@gmail.com"), List.of("test@test.com"), null,
    // WELCOME, null);
    Authentication authentication;
    UserEntity userEntity = null;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.username(), loginRequest.password()));
      userEntity = (UserEntity) authentication.getPrincipal();
    } catch (BadCredentialsException e) {
      throw new AccessDeniedException(INVALID_USERNAME_OR_PASSWORD);
    } catch (RuntimeException e) {
      log.error("Authentication failed: {}", e.getMessage());
    }
    if (userEntity == null) {
      throw new BadCredentialsException("User not found");
    }
    updateUserAfterSuccessfulLogin(userEntity, loginRequest.product());
    ApiInfoDto apiInfo = getApiInfo(userEntity, loginRequest.product());
    HashMap<String, Object> extraClaims = toExtraClaims(userEntity, loginRequest.product());
    String token =
        jwtService.generateToken(
            userEntity.getUsername(),
            extraClaims,
            TimeUnit.MINUTES.toMillis(jwtTokenValidityMinutes));
    String refreshToken =
        jwtService.generateToken(
            userEntity.getUsername(),
            extraClaims,
            TimeUnit.DAYS.toMillis(jwtRefreshTokenValidityDays));
    return AuthLoginResponse.builder()
        .token(token)
        .refreshToken(refreshToken)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .apiInfo(apiInfo)
        .build();
  }

  public AuthRefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    SessionUser sessionUser = jwtService.validateAndExtractToken(request.refreshToken());
    HashMap<String, Object> extraClaims = IJwtService.toExtraClaims(sessionUser);
    String newToken =
        jwtService.generateToken(
            sessionUser.username(),
            extraClaims,
            TimeUnit.DAYS.toMillis(jwtRefreshTokenValidityDays));
    return AuthRefreshTokenResponse.builder().token(newToken).build();
  }

  public AuthUserInfoResponse findAuthenticatedUserInfo() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AuthUserInfoResponse response = new AuthUserInfoResponse();
    UserEntity userEntityByUserName = userService.findUserEntityByUserName(sessionUser.username());
    response.setUser(UserMapper.toBase(userEntityByUserName));
    if (sessionUser.companyGroupId() != null) {
      try {
        CompanyGroup companyGroup = companyGroupService.findById(sessionUser.companyGroupId());
        response.setCompanyGroup(companyGroup);
      } catch (IllegalDefinitionException exception) {
        log.info("Company group not found: {}", sessionUser.companyGroupId());
      }
      List<Company> companyList = null;
      if (sessionUser.companyIds() != null) {
        companyList =
            companyService.findByBaseId(sessionUser.companyIds()).stream()
                .map(CompanyGroupMapper::toBase)
                .toList();
      } else {
        companyList =
            companyService.findByCompanyGroupId(sessionUser.companyGroupId()).stream()
                .map(CompanyGroupMapper::toBase)
                .toList();
      }

      response.setCompany(companyList);
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
    userDefinitionService.signEula(IJwtService.findSessionUserOrThrow().username());
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

  private HashMap<String, Object> toExtraClaims(UserEntity userEntity, ApplicationProduct product) {
    return IJwtService.toExtraClaims(userEntity, product, getResponsibleCompanyIds(userEntity));
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
    userService.updateLoginStats(userEntity.getUsername(), product);
  }
}
