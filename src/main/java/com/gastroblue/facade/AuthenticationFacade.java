package com.gastroblue.facade;

import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.*;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.SystemRole;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.request.RefreshTokenRequest;
import com.gastroblue.model.response.*;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.*;
import java.time.LocalDateTime;
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
  private final CompanyGroupProductService companyGroupProductService;
  private final UserProductService userProductService;

  @Value("${application.security.jwt.token-validity-in-minutes}")
  private Long jwtTokenValidityMinutes;

  @Value("${application.security.jwt.refresh-token-validity-in-days}")
  private Long jwtRefreshTokenValidityDays;

  public AuthLoginResponse login(AuthLoginRequest loginRequest) {
    log.info("Login request: {}", loginRequest.toString());
    UserEntity userEntity;
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.username(), loginRequest.password()));
      userEntity = (UserEntity) authentication.getPrincipal();
    } catch (BadCredentialsException e) {
      throw new AccessDeniedException(INVALID_USERNAME_OR_PASSWORD);
    } catch (RuntimeException e) {
      log.error("Authentication failed unexpectedly: {}", e.getMessage(), e);
      throw e;
    }

    UserProductEntity userProduct =
        loginRequest.product() != ApplicationProduct.ADMIN_PANEL
            ? userProductService
                .findByUserIdAndProduct(userEntity.getId(), loginRequest.product())
                .orElse(null)
            : null;

    userService.updateLoginStats(
        userEntity.getUsername(), userEntity.getId(), loginRequest.product());
    ApiInfoDto apiInfo = getApiInfo(userEntity, loginRequest.product());
    HashMap<String, Object> extraClaims =
        IJwtService.toExtraClaims(
            userEntity,
            userProduct,
            loginRequest.product(),
            getResponsibleCompanyIds(userEntity, userProduct));
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

    boolean eulaRequired =
        userProduct == null
            || userProduct.getEulaAcceptedAt() == null
            || userProduct.getEulaAcceptedAt().isBefore(LocalDateTime.now());

    return AuthLoginResponse.builder()
        .token(token)
        .refreshToken(refreshToken)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .eulaRequired(eulaRequired)
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
    UserEntity userEntityByUserName = userService.findUserByUserName(sessionUser.username());

    UserProductEntity userProduct =
        sessionUser.getApplicationProduct() != null
            ? userProductService
                .findByUserIdAndProduct(
                    userEntityByUserName.getId(), sessionUser.getApplicationProduct())
                .orElse(null)
            : null;

    response.setUser(
        UserMapper.toResponse(userEntityByUserName, userProduct, enumConfigurationFacade));
    if (sessionUser.companyGroupId() != null) {
      try {
        CompanyGroup companyGroup =
            companyGroupService.findCompanyByIdOrThrow(sessionUser.companyGroupId());
        response.setCompanyGroup(companyGroup);
      } catch (IllegalDefinitionException exception) {
        log.info("Company group not found: {}", sessionUser.companyGroupId());
      }
      List<Company> companyList;
      if (sessionUser.companyIds() != null) {
        companyList =
            companyService.findByBaseIdIn(sessionUser.companyIds()).stream()
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
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    userDefinitionService.signEula(sessionUser.username(), sessionUser.getApplicationProduct());
  }

  public EulaResponse getEula() {
    String activeEulaContent =
        eulaContentService.getActiveEulaContent(IJwtService.findSessionUserOrThrow());
    return new EulaResponse(activeEulaContent);
  }

  private ApiInfoDto getApiInfo(UserEntity userEntity, ApplicationProduct product) {
    if (userEntity.getSystemRole() == SystemRole.ADMIN
        || product == ApplicationProduct.ADMIN_PANEL) {
      return ApiInfoDto.builder().build();
    }
    return companyGroupProductService
        .findByCompanyGroupIdAndProduct(userEntity.getCompanyGroupId(), product)
        .map(p -> buildApiInfo(product, p.getEnabled(), p.getApiUrl(), p.getApiVersion()))
        .orElseThrow(() -> new AccessDeniedException(ErrorCode.ACCESS_DENIED));
  }

  private ApiInfoDto buildApiInfo(
      ApplicationProduct product, boolean enabled, String url, String version) {
    if (!enabled || url == null) {
      switch (product) {
        case FORMFLOW:
          throw new AccessDeniedException(ErrorCode.FORMFLOW_APP_NOT_ENABLED_FOR_COMPANY_GROUP);
        case TRACKER:
          throw new AccessDeniedException(
              ErrorCode.THERMOMETER_TRACKER_APP_NOT_ENABLED_FOR_COMPANY_GROUP);
        default:
          throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
      }
    }
    return ApiInfoDto.builder().url(url).version(version).build();
  }

  private List<String> getResponsibleCompanyIds(
      UserEntity userEntity, UserProductEntity userProduct) {
    if (userEntity.getSystemRole() == SystemRole.ADMIN) return List.of();
    if (userProduct == null) return List.of();
    return switch (userProduct.getProductRole()) {
      case GROUP_MANAGER -> List.of();
      case ZONE_MANAGER ->
          companyService.findByCompanyGroupId(userEntity.getCompanyGroupId()).stream()
              .map(CompanyEntity::getId)
              .toList();
      default -> List.of(userEntity.getCompanyId());
    };
  }
}
