package com.gastroblue.facade;

import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;

import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.model.properties.JwtProperties;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.util.DelimitedStringUtil;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.*;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.request.PushTokenRequest;
import com.gastroblue.model.request.RefreshTokenRequest;
import com.gastroblue.model.response.*;
import com.gastroblue.service.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

  private final IJwtService jwtService;
  private final JwtProperties jwtProperties;
  private final ILookupService lookupService;
  private final TokenGenerationService tokenGenerationService;

  private final AuthenticationManager authenticationManager;
  private final UserDefinitionService userService;
  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final CompanyGroupEulaContentService eulaContentService;
  private final CompanyGroupProductService companyGroupProductService;
  private final CompanyProductService companyProductService;
  private final UserProductService userProductService;

  public AuthLoginResponse login(AuthLoginRequest request) {
    log.info("Login request: username={}, product={}, channel={}", request.username(), request.product(), request.channel());
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
        buildExtraClaims(
            userEntity, userProduct, product, getResponsibleCompanyIds(userEntity, userProduct));
    String token =
        tokenGenerationService.generateToken(
            userEntity.getUsername(),
            extraClaims,
            TimeUnit.MINUTES.toMillis(jwtProperties.getTokenValidityInMinutes()));
    String refreshToken =
        tokenGenerationService.generateToken(
            userEntity.getUsername(),
            extraClaims,
            TimeUnit.DAYS.toMillis(jwtProperties.getRefreshTokenValidityInDays()));

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

    UserEntity userEntity = userService.findUserByUserName(sessionUser.username());
    if (userEntity == null || !userEntity.isActive()) {
      throw new AccessDeniedException(ErrorCode.UNAUTHORIZED_USER, "User is not active");
    }

    if (sessionUser.getApplicationProduct() != null) {
      UserProductEntity userProduct =
          userProductService
              .findByUserIdAndProduct(userEntity.getId(), sessionUser.getApplicationProduct())
              .orElseThrow(() -> new AccessDeniedException(ErrorCode.COMPANY_PRODUCT_NOT_FOUND));
      if (!userProduct.isActive()) {
        throw new AccessDeniedException(
            ErrorCode.COMPANY_PRODUCT_NOT_ACTIVE, "UserProduct is not active");
      }
    }

    HashMap<String, Object> extraClaims = IJwtService.toExtraClaims(sessionUser);
    String newToken =
        tokenGenerationService.generateToken(
            sessionUser.username(),
            extraClaims,
            TimeUnit.DAYS.toMillis(jwtProperties.getRefreshTokenValidityInDays()));
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

    response.setUser(UserMapper.toResponse(userEntityByUserName, userProduct, lookupService));
    if (sessionUser.companyGroupId() != null) {
      try {
        CompanyGroup companyGroup =
            companyGroupService.findCompanyByIdOrThrow(sessionUser.companyGroupId());
        response.setCompanyGroup(companyGroup);
      } catch (NotFoundException exception) {
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

  public AuthUserCompanyGroupsResponse findMyCompanyGroups() {
    return AuthUserCompanyGroupsResponse.builder()
        .companyGroups(
            companyGroupService.findMyCompanyGroups().stream()
                .map(CompanyGroupMapper::toAuthResponse)
                .toList())
        .build();
  }

  public AuthUserCompaniesResponse findMyCompanies() {
    return AuthUserCompaniesResponse.builder()
        .companies(
            companyService
                .findByCompanyGroupId(IJwtService.findSessionUserOrThrow().companyGroupId())
                .stream()
                .map(entity -> CompanyGroupMapper.toAuthResponse(entity, lookupService))
                .toList())
        .build();
  }

  public void signEula() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    UserEntity user = userService.findUserByUserName(sessionUser.username());
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
              if (Boolean.FALSE.equals(cp.isEnabled())) {
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

  private static HashMap<String, Object> buildExtraClaims(
      UserEntity userEntity,
      UserProductEntity userProduct,
      ApplicationProduct product,
      List<String> companyIds) {
    HashMap<String, Object> extraClaims = new HashMap<>();
    extraClaims.put(IJwtService.JWT_USER_ID, userEntity.getId());
    extraClaims.put(IJwtService.JWT_COMPANY_GROUP_ID, userEntity.getCompanyGroupId());
    extraClaims.put(
        IJwtService.JWT_ROLE, userProduct != null ? userProduct.getApplicationRole().name() : null);
    extraClaims.put(IJwtService.JWT_COMPANY_IDS, companyIds);
    extraClaims.put(IJwtService.JWT_APPLICATION_PRODUCT, product.name());
    extraClaims.put(IJwtService.JWT_LANGUAGE, userEntity.getLanguage().name());
    extraClaims.put(
        IJwtService.JWT_DEPARTMENTS,
        userProduct != null
            ? DelimitedStringUtil.splitClean(userProduct.getDepartments())
            : List.of());
    return extraClaims;
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
