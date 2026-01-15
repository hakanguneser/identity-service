package com.gastroblue.facade;

import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.Company;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.request.AuthLoginRequest;
import com.gastroblue.model.response.*;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.JwtService;
import com.gastroblue.service.impl.UserDefinitionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final com.gastroblue.service.EnumConfigurationService enumService;

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
    String token = jwtService.generateToken(userEntity);
    return AuthLoginResponse.builder()
        .token(token)
        .passwordChangeRequired(userEntity.isPasswordChangeRequired())
        .termsAcceptanceRequired(userEntity.isTermsAcceptanceRequired())
        .build();
  }

  public AuthUserInfoResponse findAuthenticatedUserInfo() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AuthUserInfoResponse response = new AuthUserInfoResponse();
    response.setUser(UserMapper.toBase(sessionUser));
    if (sessionUser.companyGroupId() != null) {
      try {
        CompanyGroup companyGroup = companyGroupService.findByBaseId(sessionUser.companyGroupId());
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
        .map(entity -> CompanyGroupMapper.toAuthResponse(entity, enumService))
        .toList();
  }

  public void signAgreement() {
    userDefinitionService.signAgreement(IJwtService.findSessionUserOrThrow().userId());
  }

  public AgreementResponse getAgreement() {
    return new AgreementResponse("Yemin Et Kimseye soylemicem diye !");
  }
}
