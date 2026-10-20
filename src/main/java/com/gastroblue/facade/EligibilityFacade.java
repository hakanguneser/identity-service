package com.gastroblue.facade;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.response.UserEligibilityResponse;
import com.gastroblue.service.UserDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EligibilityFacade {

  private final UserDefinitionService userDefinitionService;

  /** Checks session user can add Tracker users (supervisor+ and non-blank profile email). */
  public UserEligibilityResponse trackerAddUser() {
    SessionUser session = IJwtService.findSessionUserOrThrow();
    requireSupervisorOrAbove(session.getApplicationRole());

    UserEntity user = userDefinitionService.findUserByUserName(session.username());
    requireNonBlankEmail(user);

    return new UserEligibilityResponse(true, null);
  }

  private static void requireSupervisorOrAbove(ApplicationRole role) {
    if (role == null || !role.isSupervisorAndAbove()) {
      throw new BusinessException(ErrorCode.USER_NOT_ELIGIBLE_SUPERVISOR_OR_ABOVE);
    }
  }

  private static void requireNonBlankEmail(UserEntity user) {
    String email = user.getEmail();
    if (email == null || email.isBlank()) {
      throw new BusinessException(ErrorCode.USER_NOT_ELIGIBLE_EMAIL);
    }
  }
}
