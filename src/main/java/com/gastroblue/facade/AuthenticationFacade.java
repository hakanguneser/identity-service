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
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.request.PushTokenRequest;
import com.gastroblue.model.request.RefreshTokenRequest;
import com.gastroblue.model.response.*;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.*;
import java.time.LocalDate;
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
  private final CompanyProductService companyProductService;
  private final UserProductService userProductService;

  @Value("${application.security.jwt.token-validity-in-minutes}")
  private Long jwtTokenValidityMinutes;

  @Value("${application.security.jwt.refresh-token-validity-in-days}")
  private Long jwtRefreshTokenValidityDays;

  public AuthLoginResponse login(AuthLoginRequest request) {
    log.info("Login request: {}", request.toString());
    UserEntity userEntity;
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.username(), request.password()));
      userEntity = (UserEntity) authentication.getPrincipal();
    } catch (BadCredentialsException e) {
      throw new AccessDeniedException(INVALID_USERNAME_OR_PASSWORD);
    } catch (RuntimeException e) {
      log.error("Authentication failed unexpectedly: {}", e.getMessage(), e);
      throw e;
    }

    ApplicationProduct product = request.product();
    UserProductEntity userProduct =
        userProductService
            .findByUserIdAndProduct(userEntity.getId(), product)
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        ErrorCode.COMPANY_PRODUCT_NOT_FOUND,
                        "No product record for userId="
                            + userEntity.getId()
                            + " product="
                            + product));
    if (!userProduct.isActive()) {
      throw new AccessDeniedException(
          ErrorCode.COMPANY_PRODUCT_NOT_ACTIVE,
          "UserProduct is inactive for userId=" + userEntity.getId() + " product=" + product);
    }

    if (!userProduct.getApplicationRole().isAdministrator()) {
      validateLicense(userEntity.getCompanyId(), product);
    }

    userService.updatePasswordCheckAfterLogin(userEntity.getUsername());
    userProductService.updateLastSuccessLogin(userEntity.getId(), product);
    ApiInfoDto apiInfo = getApiInfo(userEntity, userProduct, product);
    HashMap<String, Object> extraClaims =
        IJwtService.toExtraClaims(
            userEntity, userProduct, product, getResponsibleCompanyIds(userEntity, userProduct));
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
        .eulaRequired(userProduct.getEulaAcceptedAt() == null)
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
    UserEntity user = userDefinitionService.findUserByUserName(sessionUser.username());
    eulaContentService.getActiveEulaContentForSessionUser();
    userProductService
        .findByUserIdAndProduct(user.getId(), sessionUser.getApplicationProduct())
        .orElseThrow();
    userProductService.updateEulaAcceptedAt(user.getId(), sessionUser.getApplicationProduct());
  }

  public EulaResponse getEula() {
    String activeEulaContent = eulaContentService.getActiveEulaContentForSessionUser();
    return new EulaResponse(activeEulaContent);
  }

  private void validateLicense(String companyId, ApplicationProduct product) {
    companyProductService
        .findByCompanyIdAndProduct(companyId, product)
        .ifPresent(
            cp -> {
              if (Boolean.FALSE.equals(cp.getEnabled())) {
                throw new AccessDeniedException(
                    ErrorCode.COMPANY_PRODUCT_NOT_ACTIVE,
                    "Product disabled at company level for companyId="
                        + companyId
                        + " product="
                        + product);
              }
              if (cp.getLicenseExpiresAt() != null
                  && cp.getLicenseExpiresAt().isBefore(LocalDate.now())) {
                throw new AccessDeniedException(ErrorCode.COMPANY_PRODUCT_LICENSE_EXPIRED);
              }
              if (cp.getAgreedUserCount() != null) {
                long activeCount =
                    userProductService.countActiveByCompanyIdAndProduct(companyId, product);
                if (activeCount > cp.getAgreedUserCount()) {
                  throw new AccessDeniedException(ErrorCode.LICENSE_USER_LIMIT_EXCEEDED);
                }
              }
            });
  }

  private ApiInfoDto getApiInfo(
      UserEntity userEntity, UserProductEntity userProduct, ApplicationProduct product) {
    if (userProduct.getApplicationRole().isAdministrator()) {
      return ApiInfoDto.builder().build();
    }
    return companyGroupProductService
        .findByCompanyGroupIdAndProduct(userEntity.getCompanyGroupId(), product)
        .map(p -> buildApiInfo(product, p.getEnabled(), p.getApiUrl(), p.getApiVersion()))
        .orElseThrow(() -> new AccessDeniedException(ErrorCode.COMPANY_GROUP_PRODUCT_NOT_FOUND));
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
          throw new AccessDeniedException(
              ErrorCode.PRODUCT_NOT_SELECTED,
              "Product not enabled or apiUrl missing for product=" + product);
      }
    }
    return ApiInfoDto.builder().url(url).version(version).build();
  }

  private List<String> getResponsibleCompanyIds(
      UserEntity userEntity, UserProductEntity userProduct) {
    return switch (userProduct.getApplicationRole()) {
      case ADMIN, GROUP_MANAGER -> List.of();
      case ZONE_MANAGER ->
          companyService.findByCompanyGroupId(userEntity.getCompanyGroupId()).stream()
              .map(CompanyEntity::getId)
              .toList();
      default -> List.of(userEntity.getCompanyId());
    };
  }

  public void pushToken(PushTokenRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    UserProductEntity userProduct =
        userProductService
            .findByUserIdAndProduct(sessionUser.username(), sessionUser.getApplicationProduct())
            .orElseThrow();
    userProduct.setPushToken(request.token());
    userProductService.save(userProduct);
  }
}
