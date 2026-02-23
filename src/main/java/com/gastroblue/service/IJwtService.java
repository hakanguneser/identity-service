package com.gastroblue.service;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public interface IJwtService {
  String ANONYMOUS_USER = "anonymousUser";

  String JWT_ROLE = "role";
  String JWT_COMPANY_GROUP_ID = "cgId";
  String JWT_LANGUAGE = "lang";
  String JWT_COMPANY_IDS = "cIds";
  String JWT_APPLICATION_PRODUCT = "aud";

  String generateToken(String username, HashMap<String, Object> extraClaims, long expiration);

  SessionUser validateAndExtractToken(String token) throws AccessDeniedException;

  static HashMap<String, Object> toExtraClaims(SessionUser sessionUser) {
    HashMap<String, Object> extraClaims = new HashMap<>();
    extraClaims.put(JWT_COMPANY_GROUP_ID, sessionUser.companyGroupId());
    extraClaims.put(JWT_ROLE, sessionUser.getApplicationRole());
    extraClaims.put(JWT_COMPANY_IDS, sessionUser.companyIds());
    extraClaims.put(JWT_APPLICATION_PRODUCT, sessionUser.applicationProduct());
    extraClaims.put(JWT_LANGUAGE, sessionUser.language());
    return extraClaims;
  }

  static HashMap<String, Object> toExtraClaims(
      UserEntity userEntity, ApplicationProduct product, List<String> companyIds) {
    HashMap<String, Object> extraClaims = new HashMap<>();
    extraClaims.put(JWT_COMPANY_GROUP_ID, userEntity.getCompanyGroupId());
    extraClaims.put(JWT_ROLE, userEntity.getApplicationRole());
    extraClaims.put(JWT_COMPANY_IDS, companyIds);
    extraClaims.put(JWT_APPLICATION_PRODUCT, product);
    extraClaims.put(JWT_LANGUAGE, userEntity.getLanguage().name());
    return extraClaims;
  }

  static SessionUser findSessionUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || authentication.getPrincipal() == null
        || Objects.equals(authentication.getPrincipal(), ANONYMOUS_USER)) {
      return null;
    }
    return (SessionUser) authentication.getPrincipal();
  }

  static SessionUser findSessionUserOrThrow() {
    SessionUser sessionUser = findSessionUser();
    if (sessionUser == null) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }
    return sessionUser;
  }

  static Language getSessionLanguage() {
    return Optional.ofNullable(findSessionUser())
        .map(SessionUser::getLanguage)
        .orElse(Language.defaultLang());
  }
}
